package me.liamgiraldo.litebridge.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Flings a player into the air and sets their game mode to survival
 * @deprecated This class is deprecated and will be removed in a future update
 * */
@Deprecated
public class StrikeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player)sender;
            if(args.length == 0){
                player.sendMessage("No player(s) provided. Please provide a player.");
            }
            else{
                for (String s: args
                     ) {
                    try {
                        Player target = Bukkit.getServer().getPlayerExact(s);
                        target.sendMessage("Prepare yourself to die.");
                        target.setGameMode(GameMode.SURVIVAL);
                        target.setVelocity(new Vector(0,100,0));
                    }catch(Exception e){
                        System.out.println("Player " + s + " not found.");
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }
}
