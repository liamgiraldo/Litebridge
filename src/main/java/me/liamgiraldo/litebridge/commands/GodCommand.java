package me.liamgiraldo.litebridge.commands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GodCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player)sender;
            if(player.getGameMode() == GameMode.SURVIVAL){
                player.sendMessage("Enabled god (creative) mode.");
                player.setGameMode(GameMode.CREATIVE);
            }
            else{
                player.sendMessage("Disabled god (creative) mode.");
                player.setGameMode(GameMode.SURVIVAL);
            }
        }
        return true;
    }
}
