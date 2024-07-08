package me.liamgiraldo.litebridge.controllers;

import me.liamgiraldo.litebridge.models.GUIModel;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;

public class GUIController implements Listener {
    GUIModel guiModel;
    public GUIController(GUIModel guiModel) {
        this.guiModel = guiModel;
    }

    @EventHandler
    public void onPlayerJoinLobby(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Welcome back!");
        if(event.getPlayer().getWorld().getName().equals("lobby")) {
            event.getPlayer().sendMessage("Welcome back to lobby!");
            event.getPlayer().getInventory().clear();

            ItemStack item = new ItemStack(Material.NETHER_STAR);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName("Main Menu");

            ArrayList<String> itemLore = new ArrayList<>();
            itemLore.add("Click to open the main menu");
            meta.setLore(itemLore);

            item.setItemMeta(meta);

            event.getPlayer().getInventory().addItem(item);
        }
    }

    @EventHandler
    public void onPlayerRightClickItem(PlayerInteractEvent event) {
        if(event.getItem() == null || event.getItem().getItemMeta() == null) {
            return;
        }
        ItemMeta itemMeta = event.getItem().getItemMeta();
        if(itemMeta.hasDisplayName() && "Main Menu".equals(itemMeta.getDisplayName())) {
            event.getPlayer().openInventory(this.guiModel.getMainMenu().getInventory());
        }
    }
}
