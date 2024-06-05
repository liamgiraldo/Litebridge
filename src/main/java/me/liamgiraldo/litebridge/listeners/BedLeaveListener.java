package me.liamgiraldo.litebridge.listeners;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class BedLeaveListener implements Listener {
    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent e){
        Player player = e.getPlayer();
        Location playerLocation = player.getLocation();
        TNTPrimed tnt = (TNTPrimed) playerLocation.getWorld().spawnEntity(playerLocation, EntityType.PRIMED_TNT);
        tnt.setFuseTicks(80);
    }
}
