package me.liamgiraldo.litebridge;

import me.liamgiraldo.litebridge.commands.*;
import me.liamgiraldo.litebridge.controllers.MapCreator;
import me.liamgiraldo.litebridge.listeners.BedLeaveListener;
import me.liamgiraldo.litebridge.listeners.PlayerJoinListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Litebridge extends JavaPlugin implements Listener {

    private static Litebridge plugin;
    private MapCreator mapCreator;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        mapCreator = new MapCreator();
        plugin = this;
        System.out.println("Litebridge is running.");

        getServer().getPluginManager().registerEvents(new BedLeaveListener(),this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getCommand("god").setExecutor(new GodCommand());
        getCommand("feed").setExecutor(new FeedCommand());
        getCommand("repeat").setExecutor(new RepeatCommand());
        getCommand("strike").setExecutor(new StrikeCommand());
        getCommand("setspawn").setExecutor(new SetSpawnCommand());
        getCommand("spawn").setExecutor(new SpawnCommand());
        getCommand("setjoinmessage").setExecutor(new SetMessageCommand());
        getCommand("bridgewand").setExecutor(mapCreator);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        System.out.println("Player " + event.getPlayer().getDisplayName() + "has joined");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("uuid")){
            if(sender instanceof Player){
                Player player = (Player)sender;
                player.sendMessage("Your UUID is " + player.getUniqueId().toString() + ".");
            }
        }
        else{
            return false;
        }
        return true;
    }

    public static Litebridge getPlugin(){
        return plugin;
    }
}
