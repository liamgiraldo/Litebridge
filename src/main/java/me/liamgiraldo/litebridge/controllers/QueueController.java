package me.liamgiraldo.litebridge.controllers;

import com.sun.tools.javac.util.List;
import me.liamgiraldo.litebridge.models.QueueStorage;
import me.liamgiraldo.litebridge.models.SignModel;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Vector;

public class QueueController implements EventListener {
    private QueueStorage queues;
    public QueueController(QueueStorage queues){
        this.queues = queues;
    }
    @EventHandler
    public void onSignRightClick(PlayerInteractEvent e){
        if(e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.SIGN){
            //how the fuck do I get the game associated with that sign
        }
    }

    @EventHandler
    public void onGameSignPlaced(BlockPlaceEvent e){
        if(e.getBlockPlaced().getType() == Material.SIGN){
            Sign sign = (Sign)e.getBlockPlaced();
            /*SignModel signModel = new SignModel()*/
        }
    }

    @EventHandler
    public void onSignWritten(SignChangeEvent e){
        Sign sign = (Sign)e.getBlock();
        String[] signLines = sign.getLines();
        String mapName, playerCount;
        World world;
        Player player = e.getPlayer();
        try{
            mapName = signLines[0];
            playerCount = signLines[1];
        }catch(Exception exception){
            player.sendMessage("Looks like this bridge sign is invalid.");
            player.sendMessage("The first line has to be a valid world name for the bridge map.");
            player.sendMessage("The second line has to be the maximum player count for that bridge game.");
            player.sendMessage("The last lines don't matter. They will be filled in.");
            exception.printStackTrace();
            return;
        }
        try{
            world = Bukkit.getWorld(mapName);
        }catch(Exception exception){
            player.sendMessage("Looks like the world specified by this sign does not exist. Try again.");
            return;
        }
        //get the game model for that given world
//        SignModel signModel = new SignModel(world, );
    }
}
