package me.liamgiraldo.litebridge.controllers;

import me.liamgiraldo.litebridge.models.CustomItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class LobbyManager implements Listener {
    private Location lobby;
    private CustomItem mainMenu;

    public LobbyManager(Location lobby){
        this.lobby = lobby;
        this.mainMenu = new CustomItem("Main Menu", new ItemStack(Material.NETHER_STAR));
        mainMenu.setDisplayName("Main Menu");
        mainMenu.addLore("Click to open the main menu");

    }

    @EventHandler
    public void onPlayerJoinLobby(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Welcome back!");
        if(event.getPlayer().getWorld().getName().equals(lobby.getWorld().getName())) {
            event.getPlayer().sendMessage("Welcome back to Litebridge!");
            event.getPlayer().getInventory().clear();

            if(event.getPlayer().getInventory().contains(mainMenu.getItemStack())){
                return;
            }

            event.getPlayer().getInventory().addItem(mainMenu.getItemStack());
        }
    }

    @EventHandler
    public void onPlayerTeleportToLobby(PlayerTeleportEvent event) {
        if(event.getTo().getWorld().getName().equals(lobby.getWorld().getName())) {
            event.getPlayer().sendMessage("Welcome back to Litebridge!");
            event.getPlayer().getInventory().clear();

            if(event.getPlayer().getInventory().contains(mainMenu.getItemStack())){
                return;
            }

            event.getPlayer().getInventory().addItem(mainMenu.getItemStack());
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        //if the player is in the lobby, cancel the event
        if(event.getPlayer().getWorld().getName().equals(lobby.getWorld().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(player.getWorld().getName().equals(lobby.getWorld().getName())) {
                event.setCancelled(true);
            }
        }
    }

    public Location getLobby(){
        return lobby;
    }
}
