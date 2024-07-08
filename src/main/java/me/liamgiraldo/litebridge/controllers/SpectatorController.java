package me.liamgiraldo.litebridge.controllers;

import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.events.ForceStartEvent;
import me.liamgiraldo.litebridge.events.GameEndEvent;
import me.liamgiraldo.litebridge.events.QueueFullEvent;
import me.liamgiraldo.litebridge.models.GameModel;
import me.liamgiraldo.litebridge.models.SpectatorQueueModel;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class SpectatorController implements CommandExecutor, Listener {
    ArrayList<SpectatorQueueModel> spectatorQueues;
    Location lobby;
    Litebridge plugin;
    public SpectatorController(ArrayList<SpectatorQueueModel> spectatorQueues, Location lobby, Litebridge plugin) {
        this.spectatorQueues = spectatorQueues;
        this.lobby = lobby;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            /**
             * The available arguments for spectator commands are:
             * - map name
             * - leave
             * */
            switch(args[0]){
                case "leave":
                    for(SpectatorQueueModel queue : spectatorQueues){
                        if(queue.getSpectators().contains(p)){
                            queue.removeSpectator(p);
                            p.teleport(lobby);

                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    p.setGameMode(GameMode.SURVIVAL);
                                }
                            }.runTaskLater(plugin, 20);

                            p.sendMessage("You are no longer spectating " + queue.getWorld().getName());
                            return true;
                        }
                    }
                    p.sendMessage("You are not in a spectator queue");
                    return true;
                case "join":
                    if(args.length < 2){
                        p.sendMessage("Please specify a world to spectate");
                        return true;
                    }
                    for(SpectatorQueueModel queue : spectatorQueues){
                        if(queue.getWorld().getName().equals(args[1])){
                            if(queue.getAssociatedGame().getGameState() != GameModel.GameState.ACTIVE){
                                p.sendMessage("You cannot spectate a game that is not active");
                                return true;
                            }
                            queue.addSpectator(p);
                            p.teleport(queue.getWorld().getSpawnLocation());

                            //Use a bukkit runnable to set their gamemode to spectator after they have been teleported
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    p.setGameMode(GameMode.SPECTATOR);
                                }
                            }.runTaskLater(plugin, 20);

                            p.sendMessage("You are now spectating " + queue.getWorld().getName());
                            return true;
                        }
                    }
                    p.sendMessage("That world does not exist");
                    return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onQueueFullEvent(QueueFullEvent e){
        for(SpectatorQueueModel queue : spectatorQueues){
            if(queue.getWorld().equals(e.getQueue().getWorld())){
                for(Player p : queue.getSpectators()){
                    p.teleport(queue.getWorld().getSpawnLocation());
                    p.setGameMode(GameMode.SPECTATOR);
                    p.sendMessage("The game you were spectating has started");
                }
            }
        }
    }

    @EventHandler
    public void onGameForceStart(ForceStartEvent e){
        for(SpectatorQueueModel queue : spectatorQueues){
            if(queue.getWorld().equals(e.getQueue().getWorld())){
                for(Player p : queue.getSpectators()){
                    p.teleport(queue.getWorld().getSpawnLocation());
                    p.setGameMode(org.bukkit.GameMode.SPECTATOR);
                    p.sendMessage("The game you were spectating has started");
                }
            }
        }
    }

    @EventHandler
    public void onGameEnd(GameEndEvent e){
        for(SpectatorQueueModel queue : spectatorQueues){
            if(queue.getAssociatedGame().equals(e.getGame())){
                for(Player p : queue.getSpectators()){
                    p.teleport(lobby);
                    p.setGameMode(org.bukkit.GameMode.SURVIVAL);
                    p.sendMessage("The game you were spectating has ended");
                }
            }
        }
    }
}
