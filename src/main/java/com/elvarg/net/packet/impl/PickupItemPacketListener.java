package com.elvarg.net.packet.impl;

import java.util.Optional;

import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.grounditem.ItemOnGround;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager.OperationType;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.movement.WalkToAction;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

/**
 * This packet listener is used to pick up ground items that exist in the world.
 *
 * @author relex lawl
 */

public class PickupItemPacketListener implements PacketExecutor {

	@Override
	public void execute(final Player player, Packet packet) {
		final int y = packet.readLEShort();
		final int itemId = packet.readShort();
		final int x = packet.readLEShort();
		final Location position = new Location(x, y, player.getLocation().getZ());

		if (player.getRights() == PlayerRights.DEVELOPER) {
			player.getPacketSender()
					.sendMessage("Pick up item: " + Integer.toString(itemId) + ". " + position.toString());
		}

        if (player.busy()) {
            return;
        }

        if (!player.getLastItemPickup().elapsed(300))
            return;

        player.setWalkToTask(new WalkToAction(player) {
			@Override
			public void execute() {
				// Make sure distance isn't way off..
				if (Math.abs(player.getLocation().getX() - x) > 25 || Math.abs(player.getLocation().getY() - y) > 25) {
					player.getMovementQueue().reset();
					return;
				}

				// Check if we can hold it..
				if (!(player.getInventory().getFreeSlots() > 0 || (player.getInventory().getFreeSlots() == 0
						&& ItemDefinition.forId(itemId).isStackable() && player.getInventory().contains(itemId)))) {
					player.getInventory().full();
					return;
				}

				Optional<ItemOnGround> item = ItemOnGroundManager.getGroundItem(Optional.of(player.getUsername()),
						itemId, position);
				boolean deregister = true;
				if (item.isPresent()) {
					if (player.getInventory().getAmount(item.get().getItem().getId())
							+ item.get().getItem().getAmount() > Integer.MAX_VALUE
							|| player.getInventory().getAmount(item.get().getItem().getId())
									+ item.get().getItem().getAmount() <= 0) {
						int playerCanHold = Integer.MAX_VALUE
								- player.getInventory().getAmount(item.get().getItem().getId());
						if (playerCanHold <= 0) {
							player.getPacketSender().sendMessage("You cannot hold that more of that item.");
							return;
						} else {
							int currentAmount = item.get().getItem().getAmount();
							item.get().setOldAmount(currentAmount);
							item.get().getItem().decrementAmountBy(playerCanHold);
							ItemOnGroundManager.perform(item.get(), OperationType.ALTER);
							deregister = false;
						}
					}
					if (deregister) {
						ItemOnGroundManager.deregister(item.get());
					}
					player.getInventory().add(item.get().getItem());
					player.getLastItemPickup().reset();
				}
			}
			
			@Override
            public boolean inDistance() {
                return player.getLocation().equals(position);
            }
		});
	}
}
