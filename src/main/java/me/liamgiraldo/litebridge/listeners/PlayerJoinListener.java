package me.liamgiraldo.litebridge.listeners;

import me.liamgiraldo.litebridge.Litebridge;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerJoinListener implements Listener {
//    @EventHandler
//    public void onJoinEven(PlayerJoinEvent e){
//        Player player = e.getPlayer();
//        String joinMessage = Litebridge.getPlugin().getConfig().getString("join-message");
//        if(joinMessage != null){
//            joinMessage = joinMessage.replace("%player%", player.getDisplayName());
//            player.sendMessage(ChatColor.translateAlternateColorCodes('&', joinMessage));
//        }
//        Location spawnLocation = (Location) Litebridge.getPlugin().getConfig().get("spawn");
//        if (spawnLocation == null || spawnLocation.getWorld() == null) {
//            player.sendMessage("The spawn location is invalid. Ensure the world exists and the coordinates are correct.");
//        }
//        else if(!player.hasPlayedBefore()){
//            player.teleport(spawnLocation);
//        }
//    }

    public void onPlayerRespawn(PlayerRespawnEvent e){
        Player player = e.getPlayer();
        Location spawnLocation = (Location) Litebridge.getPlugin().getConfig().get("spawn");
        if (spawnLocation == null || spawnLocation.getWorld() == null) {
            player.sendMessage("The spawn location is invalid. Ensure the world exists and the coordinates are correct.");
        }
        else{
            e.setRespawnLocation(spawnLocation);
        }
    }
}
