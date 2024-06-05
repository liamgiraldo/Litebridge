package me.liamgiraldo.litebridge.commands;

import me.liamgiraldo.litebridge.Litebridge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class SpawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Directly get the Location object from the config
            try {
                Location spawnLocation = (Location) Litebridge.getPlugin().getConfig().get("spawn");
                if (spawnLocation == null || spawnLocation.getWorld() == null) {
                    player.sendMessage("The spawn location is invalid. Ensure the world exists and the coordinates are correct.");
                    return true;
                }

                player.teleport(spawnLocation);
                player.sendMessage("Teleported to the spawn location.");
            } catch (Exception e) {
                player.sendMessage("Something went wrong parsing the spawn location. Tell a programmer!");
                e.printStackTrace(); // This will log the error details to the console
            }
        }
        return true;
    }
}
