package me.liamgiraldo.litebridge.controllers;

import com.samjakob.spigui.buttons.SGButton;
import me.liamgiraldo.litebridge.models.GUIModel;
import me.liamgiraldo.litebridge.models.GameModel;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import sun.jvm.hotspot.opto.Block;

import java.util.ArrayList;

public class GUIController implements Listener {
    GUIModel guiModel;
    ArrayList<GameModel> games;
    public GUIController(GUIModel guiModel, ArrayList<GameModel> games) {
        this.guiModel = guiModel;
        this.games = games;

//        generateMapButtons();
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

    private void generateMapButtons(){
        int i = 0;
        for(GameModel game: games){
            ItemStack item = new ItemStack(Material.MAP);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(removeNumbers(game.getWorld().getName()));
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.GOLD + "In-Game:");
            for(Player p: game.getPlayers()){
                if(p == null){
                    lore.add(ChatColor.GRAY + "Empty");
                    continue;
                }
                ChatColor color;
                if(game.checkIfPlayerIsInRedTeam(p)){
                    color = ChatColor.RED;
                } else {
                    color = ChatColor.BLUE;
                }
                lore.add(color + p.getName());
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            SGButton button = new SGButton(item).withListener((InventoryClickEvent event) -> {
                event.getWhoClicked().closeInventory();
                if(event.getWhoClicked() instanceof Player){
                    Player p = (Player) event.getWhoClicked();
                    String endOfWorldName;
                    if(!hasNumber(game.getWorld().getName())){
                        endOfWorldName = "";
                    } else{
                        endOfWorldName = Integer.toString(this.guiModel.getLastSelectedMode(p));
                    }

                    p.performCommand("q " + this.guiModel.getLastSelectedMode(p) + " " + game.getWorld().getName() + endOfWorldName);
                }
            });

            this.guiModel.getMapmenu().setButton(i, button);
            i++;
        }
    }

    private String removeNumbers(String str) {
        return str.replaceAll("\\d", "");
    }

    //write a method to check if a string has a number in it
    private boolean hasNumber(String s) {
        return s.matches(".*\\d.*");
    }
}
