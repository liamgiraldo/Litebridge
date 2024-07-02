package me.liamgiraldo.litebridge.commands;

import me.liamgiraldo.litebridge.Litebridge;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sets the join message for the server
 * @deprecated This will be removed in a future update, this is not the responsibility of the plugin
 */
@Deprecated
public class SetMessageCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player)sender;
            if(args.length > 0){
                StringBuilder joinMessage = new StringBuilder();
                for (String s:
                     args) {
                    joinMessage.append(s);
                    joinMessage.append(" ");
                }
                Litebridge.getPlugin().getConfig().set("join-message",joinMessage.toString());
            }else{
                player.sendMessage("Please provide a message.");
            }
        }
        return true;
    }
}
