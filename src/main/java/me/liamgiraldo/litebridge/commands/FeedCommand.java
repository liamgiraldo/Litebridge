package me.liamgiraldo.litebridge.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class FeedCommand implements CommandExecutor {
    private final HashMap<UUID, Long> cooldowns;
    public FeedCommand(){
        cooldowns = new HashMap<UUID, Long>();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player)sender;
            if(!cooldowns.containsKey(player.getUniqueId())){
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                feed(player);
            }else{
                //the current time in milliseconds - the current time it was when the player used the command
                long elapsedTime = (System.currentTimeMillis() - cooldowns.get(player.getUniqueId()));
                if(elapsedTime > 100000){
                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                    feed(player);
                }
                else{
                    long timeLeft = (100000 - elapsedTime)/1000;
                    player.sendMessage("You can't use that for another " + timeLeft + " seconds");
                }
            }
        }
        return true;
    }
    private void feed(Player player){
        player.setFoodLevel(20);
        player.sendMessage("Eat up.");
    }
}
