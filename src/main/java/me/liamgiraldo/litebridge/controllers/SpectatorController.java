package me.liamgiraldo.litebridge.controllers;

import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.events.ForceStartEvent;
import me.liamgiraldo.litebridge.events.GameEndEvent;
import me.liamgiraldo.litebridge.events.QueueFullEvent;
import me.liamgiraldo.litebridge.models.GameModel;
import me.liamgiraldo.litebridge.models.SpectatorQueueModel;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

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
                    for(SpectatorQueueModel queue: spectatorQueues){
                        //if the player is already spectating a game, don't let them join another
                        if(queue.getSpectators().contains(p)){
                            p.sendMessage("You are already spectating a game");
                            return true;
                        }
                    }

                    for(SpectatorQueueModel queue : spectatorQueues){
                        if(queue.getWorld().getName().equals(args[1])){
                            if(queue.getAssociatedGame().getGameState() != GameModel.GameState.ACTIVE){
                                p.sendMessage("You cannot spectate a game that is not active");
                                return true;
                            }
                            for(Player player : queue.getAssociatedQueue().getQueue()){
                                if(player == null){
                                    continue;
                                }
                                if(player.equals(p)){
                                    p.sendMessage("You cannot spectate a game you are in / queueing for!");
                                    return true;
                                }
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
                    p.getInventory().clear();
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
                    p.getInventory().clear();
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
                    queue.removeSpectator(p);
                    p.getInventory().clear();
                    clearPlayerScoreboard(p);
                }
            }
        }
    }

    @EventHandler
    public void onServerLeave(PlayerQuitEvent e){
        for(SpectatorQueueModel queue : spectatorQueues){
            if(queue.getSpectators().contains(e.getPlayer())){
                queue.removeSpectator(e.getPlayer());
                e.getPlayer().teleport(lobby);
                e.getPlayer().setGameMode(GameMode.SURVIVAL);
                clearPlayerScoreboard(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onSpectatorMoveEvent(PlayerMoveEvent e){
        for(SpectatorQueueModel queue : spectatorQueues){
            //if the spectator goes out of bounds of the world they are spectating, teleport them back to the spawn
            if(queue.getSpectators().contains(e.getPlayer())){
                if(queue.getAssociatedGame().getGameState() != GameModel.GameState.ACTIVE){
                    return;
                }
                GameModel game = queue.getAssociatedGame();
                int[][] bounds = game.getWorldBounds();
                int[] bounds1 = bounds[0];
                int[] bounds2 = bounds[1];
                int minX = Math.min(bounds1[0], bounds2[0]);
                int minY = Math.min(bounds1[1], bounds2[1]);
                int minZ = Math.min(bounds1[2], bounds2[2]);
                int maxX = Math.max(bounds1[0], bounds2[0]);
                int maxY = Math.max(bounds1[1], bounds2[1]);
                int maxZ = Math.max(bounds1[2], bounds2[2]);
                //add 100 block buffer to the bounds
                minX -= 100;
                minY -= 100;
                minZ -= 100;
                maxX += 100;
                maxY += 100;
                maxZ += 100;
                if(e.getTo().getBlockX() < minX || e.getTo().getBlockX() > maxX || e.getTo().getBlockY() < minY || e.getTo().getBlockY() > maxY || e.getTo().getBlockZ() < minZ || e.getTo().getBlockZ() > maxZ){
                    e.getPlayer().teleport(queue.getWorld().getSpawnLocation());
                    e.getPlayer().sendMessage("You cannot leave the bounds of the game you are spectating");
                }
            }
        }
    }

    public void clearPlayerScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null) {
            Scoreboard blankScoreboard = manager.getNewScoreboard();
            player.setScoreboard(blankScoreboard);
        }
    }
}
