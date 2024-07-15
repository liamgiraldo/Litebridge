package me.liamgiraldo.litebridge.controllers;

import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.models.CustomItem;
import me.liamgiraldo.litebridge.models.GameModel;
import me.liamgiraldo.litebridge.models.QueueModel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class LobbyManager implements CommandExecutor, Listener {
    private Location lobby;
    private CustomItem mainMenu;
    private Litebridge plugin;
    private ArrayList<QueueModel> queues;
    private GameController gameController;

    public LobbyManager(Location lobby, Litebridge plugin, ArrayList<QueueModel> queues, GameController gameController) {
        this.lobby = lobby;
        this.mainMenu = new CustomItem("Main Menu", new ItemStack(Material.NETHER_STAR));
        mainMenu.setDisplayName("Main Menu");
        mainMenu.addLore("Click to open the main menu");
        this.queues = queues;
        this.gameController = gameController;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinLobby(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Welcome back!");
        if(event.getPlayer().getWorld().getName().equals(lobby.getWorld().getName())) {
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
            event.getPlayer().getInventory().clear();

            if(event.getPlayer().getInventory().contains(mainMenu.getItemStack())){
                return;
            }

            event.getPlayer().getInventory().addItem(mainMenu.getItemStack());

            //if the player is in a game, remove them from the game
            for(QueueModel queue: queues){
                if(queue.isPlayerInQueue(event.getPlayer())){
                    GameModel game = queue.getAssociatedGame();
                    game.removePlayer(event.getPlayer());
                    queue.removeFromQueue(event.getPlayer());
                    if(game.checkIfGameIsEmpty()){
                        gameController.gameEndInstantly(game);
                    }
                }
            }
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

    @EventHandler
    public void onHungerLoss(FoodLevelChangeEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(player.getWorld().getName().equals(lobby.getWorld().getName())) {
                player.setFoodLevel(20);
                player.setSaturation(20);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMobSpawnEvent(CreatureSpawnEvent event) {
        if(event.getEntity().getWorld().getName().equals(lobby.getWorld().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(event.getPlayer().getWorld().getName().equals(lobby.getWorld().getName())) {
            //also if the player doesn't have a bridge star, give them one
            if(!event.getPlayer().getInventory().contains(mainMenu.getItemStack())){
                event.getPlayer().getInventory().addItem(mainMenu.getItemStack());
            }
            //if the player goes below the lobby-kill-layer defined in the config, place them back at spawn
            int killLayer = (int)plugin.getConfig().get("lobby-kill-layer");
            if(event.getPlayer().getLocation().getY() <= killLayer){
                event.getPlayer().teleport(lobby);
            }
        }
    }

    public Location getLobby(){
        return lobby;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player p = (Player)sender;
            p.teleport(lobby);
            for(QueueModel queue: queues){
                if(queue.isPlayerInQueue(p)){
                    GameModel game = queue.getAssociatedGame();
                    game.removePlayer(p);
                    queue.removeFromQueue(p);
                }
            }
            p.getInventory().clear();
            p.getInventory().setArmorContents(null);
            p.sendMessage("Sending you the bridge lobby.");
        }
        return false;
    }
}
