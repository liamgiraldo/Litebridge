package me.liamgiraldo.litebridge.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RepeatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player)sender;
            if(args.length == 0){
                player.sendMessage("You didn't provide any arguments");
            }
            else if(args.length == 1){
                String word = args[0];
                player.sendMessage(word);
            }
            else{
                StringBuilder builder = new StringBuilder();
                for (String arg : args) {
                    builder.append(arg);
                    builder.append("");
                }
                String finalMessage = builder.toString();
                player.sendMessage(finalMessage);
            }
        }
        return true;
    }
}
