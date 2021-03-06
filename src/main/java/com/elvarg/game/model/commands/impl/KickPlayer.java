package com.elvarg.game.model.commands.impl;

import java.util.Optional;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class KickPlayer implements Command {

	@Override
	public void execute(Player player, String command, String[] parts) {
		Optional<Player> plr = World.getPlayerByName(command.substring(parts[0].length() + 1));
        plr.ifPresent(Player::requestLogout);
	}

	@Override
	public boolean canUse(Player player) {
		PlayerRights rights = player.getRights();
		return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
	}

}
