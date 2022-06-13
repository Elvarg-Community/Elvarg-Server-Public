package com.elvarg.game.model.commands.impl;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.player.PlayerSaving;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.util.PlayerPunishment;

import java.util.Optional;

public class UnMutePlayer implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        String player2 = command.substring(parts[0].length() + 1);
        Optional<Player> plr = World.getPlayerByName(player2);

        if (!PlayerSaving.playerExists(player2) && !plr.isPresent()) {
            player.getPacketSender().sendMessage("Player " + player2 + " does not exist.");
            return;
        }

        if (!PlayerPunishment.muted(player2)) {
            player.getPacketSender().sendMessage("Player " + player2 + " does not have an active mute.");
            return;
        }

    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

}
