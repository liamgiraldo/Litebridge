package me.liamgiraldo.litebridge.commands;

import me.liamgiraldo.litebridge.Litebridge;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player)sender;
            Location location = player.getLocation();
//            Litebridge.getPlugin().getConfig().set("spawn.x",location.getX());
//            Litebridge.getPlugin().getConfig().set("spawn.y",location.getY());
//            Litebridge.getPlugin().getConfig().set("spawn.z",location.getZ());
//            Litebridge.getPlugin().getConfig().set("spawn.worldName",location.getWorld().getName());
            Litebridge.getPlugin().getConfig().set("spawn", location);
            Litebridge.getPlugin().saveConfig();
            player.sendMessage("Spawn location set");
        }
        return true;
    }
}
