package com.elvarg.net.packet.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.elvarg.Server;
import com.elvarg.game.World;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.skill.skillable.impl.Cooking;
import com.elvarg.game.content.skill.skillable.impl.Cooking.Cookable;
import com.elvarg.game.content.skill.skillable.impl.Crafting;
import com.elvarg.game.content.skill.skillable.impl.Firemaking;
import com.elvarg.game.content.skill.skillable.impl.Firemaking.LightableLog;
import com.elvarg.game.content.skill.skillable.impl.Fletching;
import com.elvarg.game.content.skill.skillable.impl.Herblore;
import com.elvarg.game.content.skill.skillable.impl.Prayer.AltarOffering;
import com.elvarg.game.content.skill.skillable.impl.Prayer.BuriableBone;
import com.elvarg.game.entity.impl.grounditem.ItemOnGround;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.MapObjects;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.menu.CreationMenu;
import com.elvarg.game.model.movement.WalkToAction;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketConstants;
import com.elvarg.net.packet.PacketExecutor;
import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.ObjectIdentifiers;


public class UseItemPacketListener extends ItemIdentifiers implements PacketExecutor {

    private static void itemOnItem(Player player, Packet packet) {
        int usedWithSlot = packet.readUnsignedShort();
        int itemUsedSlot = packet.readUnsignedShortA();
        if (usedWithSlot < 0 || itemUsedSlot < 0
                || itemUsedSlot >= player.getInventory().capacity()
                || usedWithSlot >= player.getInventory().capacity())
            return;
        Item used = player.getInventory().getItems()[itemUsedSlot];
        Item usedWith = player.getInventory().getItems()[usedWithSlot];

        player.getPacketSender().sendInterfaceRemoval();
        player.getSkillManager().stopSkillable();

        //Herblore
        if (Herblore.makeUnfinishedPotion(player, used.getId(), usedWith.getId())
                || Herblore.finishPotion(player, used.getId(), usedWith.getId())
                || Herblore.concatenate(player, used, usedWith)) {
            return;
        }

        //Fletching
        if (Fletching.fletchLog(player, used.getId(), usedWith.getId())
                || Fletching.stringBow(player, used.getId(), usedWith.getId())
                || Fletching.fletchAmmo(player, used.getId(), usedWith.getId())
                || Fletching.fletchCrossbow(player, used.getId(), usedWith.getId())) {
            return;
        }

        //Crafting
        if (Crafting.craftGem(player, used.getId(), usedWith.getId())) {
            return;
        }

        //Firemaking
        if (Firemaking.init(player, used.getId(), usedWith.getId())) {
            return;
        }

        //Granite clamp on Granite maul
        if ((used.getId() == GRANITE_CLAMP || usedWith.getId() == GRANITE_CLAMP)
                && (used.getId() == GRANITE_MAUL || usedWith.getId() == GRANITE_MAUL)) {
            if (player.busy() || CombatFactory.inCombat(player)) {
                player.getPacketSender().sendMessage("You cannot do that right now.");
                return;
            }
            if (player.getInventory().contains(GRANITE_MAUL)) {
                player.getInventory().delete(GRANITE_MAUL, 1).delete(GRANITE_CLAMP, 1).add(GRANITE_MAUL_3, 1);
                player.getPacketSender().sendMessage("You attach your Granite clamp onto the maul..");
            }
            return;
        }

        //Blowpipe reload
        else if (used.getId() == TOXIC_BLOWPIPE || usedWith.getId() == TOXIC_BLOWPIPE) {
            int reload = used.getId() == TOXIC_BLOWPIPE ? usedWith.getId() : used.getId();
            if (reload == ZULRAHS_SCALES) {
                int amount = player.getInventory().getAmount(12934);
                player.incrementBlowpipeScales(amount);
                player.getInventory().delete(ZULRAHS_SCALES, amount);
                player.getPacketSender().sendMessage("You now have " + player.getBlowpipeScales() + " Zulrah scales in your blowpipe.");
            } else {
                player.getPacketSender().sendMessage("You cannot load the blowpipe with that!");
            }
        }
    }

    private static void itemOnNpc(Player player, Packet packet) {
        int id = packet.readShortA();
        int index = packet.readShortA();
        int slot = packet.readLEShort();
        if (index < 0 || index > World.getNpcs().capacity()) {
            return;
        }
        if (slot < 0 || slot > player.getInventory().getItems().length) {
            return;
        }
        NPC npc = World.getNpcs().get(index);
        if (npc == null) {
            return;
        }
        if (player.getInventory().getItems()[slot].getId() != id) {
            return;
        }
        switch (id) {

        }
    }

    @SuppressWarnings("unused")
    private static void itemOnObject(Player player, Packet packet) {
        int interfaceType = packet.readShort();
        int objectId = packet.readInt();
        int objectY = packet.readLEShortA();
        int itemSlot = packet.readLEShort();
        int objectX = packet.readLEShortA();
        int itemId = packet.readShort();

        if (itemSlot < 0 || itemSlot >= player.getInventory().capacity())
            return;
        Item item = player.getInventory().getItems()[itemSlot];
        if (item == null || item.getId() != itemId)
            return;
        Location position = new Location(objectX, objectY, player.getLocation().getZ());
        GameObject object = MapObjects.get(player, objectId, position);

        // Make sure the object actually exists in the region...
        if (object == null) {
            return;
        }

        //Update facing..
        player.setPositionToFace(position);

        //Handle object..
        switch (object.getId()) { //Edgeville Stove
            //Player-made Fire
            case ObjectIdentifiers.STOVE_4, ObjectIdentifiers.FIRE_5, ObjectIdentifiers.FIRE_23 -> { //Barb village fire
                //Handle cooking on objects..
                Optional<Cookable> cookable = Cookable.getForItem(item.getId());
                if (cookable.isPresent()) {
                    player.getPacketSender().sendCreationMenu(new CreationMenu("How many would you like to cook?", List.of(cookable.get().getCookedItem()), (productId, amount) -> {
                        player.getSkillManager().startSkillable(new Cooking(object, cookable.get(), amount));
                    }));
                    return;
                }
                //Handle bonfires..
                if (object.getId() == ObjectIdentifiers.FIRE_5) {
                    Optional<LightableLog> log = LightableLog.getForItem(item.getId());
                    if (log.isPresent()) {
                        player.getPacketSender().sendCreationMenu(new CreationMenu("How many would you like to burn?", List.of(log.get().getLogId()), (productId, amount) -> {
                            player.getSkillManager().startSkillable(new Firemaking(log.get(), object, amount));
                        }));
                        return;
                    }
                }
            }
            case 409 -> { //Bone on Altar
                Optional<BuriableBone> b = BuriableBone.forId(item.getId());
                b.ifPresent(buriableBone -> player.getPacketSender().sendCreationMenu(new CreationMenu("How many would you like to offer?", List.of(itemId), (productId, amount) -> {
                    player.getSkillManager().startSkillable(new AltarOffering(buriableBone, object, amount));
                })));
            }
        }
    }

    @SuppressWarnings("unused")
    private static void itemOnPlayer(Player player, Packet packet) {
        int interfaceId = packet.readUnsignedShortA();
        int targetIndex = packet.readUnsignedShort();
        int itemId = packet.readUnsignedShort();
        int slot = packet.readLEShort();
        if (slot < 0 || slot >= player.getInventory().capacity() || targetIndex >= World.getPlayers().capacity())
            return;
        Player target = World.getPlayers().get(targetIndex);
        if (target == null) {
            return;
        }
    }

    @SuppressWarnings("unused")
    private static void itemOnGroundItem(Player player, Packet packet) {
        int interfaceType = packet.readLEShort();
        int usedItemId = packet.readShortA();
        int groundItemId = packet.readShort();
        int y = packet.readShortA();
        int unknown = packet.readLEShortA();
        int x = packet.readShort();

        //Verify item..
        if (!player.getInventory().contains(usedItemId)) {
            return;
        }

        //Verify ground item..
        Optional<ItemOnGround> groundItem = ItemOnGroundManager.getGroundItem(Optional.of(player.getUsername()), groundItemId, new Location(x, y));
        if (groundItem.isEmpty()) {
            return;
        }

        player.setWalkToTask(new WalkToAction(player) {
            @Override
            public void execute() {
                //Face...
                player.setPositionToFace(groundItem.get().getPosition());

                //Handle used item..
                switch (usedItemId) {
                    case TINDERBOX -> { //Lighting a fire..
                        Optional<LightableLog> log = LightableLog.getForItem(groundItemId);
                        if (log.isPresent()) {
                            player.getSkillManager().startSkillable(new Firemaking(log.get(), groundItem.get()));
                            return;
                        }
                    }
                }
            }
            
            @Override
            public boolean inDistance() {
                return player.getLocation().getDistance(groundItem.get().getPosition()) <= 1;
            }
        });
    }


    @Override
    public void execute(Player player, Packet packet) {
        if (player.getHitpoints() <= 0)
            return;
        switch (packet.getOpcode()) {
            case PacketConstants.ITEM_ON_ITEM -> itemOnItem(player, packet);
            case PacketConstants.ITEM_ON_OBJECT -> itemOnObject(player, packet);
            case PacketConstants.ITEM_ON_GROUND_ITEM -> itemOnGroundItem(player, packet);
            case PacketConstants.ITEM_ON_NPC -> itemOnNpc(player, packet);
            case PacketConstants.ITEM_ON_PLAYER -> itemOnPlayer(player, packet);
        }
    }
}