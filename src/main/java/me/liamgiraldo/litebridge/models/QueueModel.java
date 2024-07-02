package me.liamgiraldo.litebridge.models;

import me.liamgiraldo.litebridge.Litebridge;
import me.liamgiraldo.litebridge.events.QueueFullEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

public class QueueModel {
    private Player[] queue;

    /**
     * This means the max players given both teams. Total players in a game.
     * To get the amount on one team, divide by two.
     * For best results this should be an even number.
     * */
    private int maxPlayers;
    private String worldName;

    private GameModel associatedGame;
    private World world;

    //5 seconds (integer)
    private int startTimer;

    public QueueModel(int maxPlayers, String worldName, GameModel associatedGame){
        this.maxPlayers = maxPlayers;
        this.queue = new Player[this.maxPlayers];
        this.associatedGame = associatedGame;
        this.world = associatedGame.getWorld();
        this.worldName = world.getName();
        this.startTimer = 5;
    }

    public Player[] getQueue() {
        return queue;
    }

    public void setQueue(Player[] queue) {
        this.queue = queue;
    }

    public int getStartTimer() {
        return startTimer;
    }

    public void setStartTimer(int startTimer) {
        this.startTimer = startTimer;
    }

    /**
     * Appends a player to an available space in the queue
     * This is the first empty spot in the array starting from the 0th index
     *
     * @param player The player to add to the queue
     * */
    public synchronized void appendToQueue(Player player) {
        for (int i = 0; i < queue.length; i++) {
            if (queue[i] == null) {
                queue[i] = player;
                break;
            }
        }
        if (isQueueFull()) {
            QueueFullEvent event = new QueueFullEvent(this);
            Bukkit.getServer().getPluginManager().callEvent(event);
        }
    }

    /**
     * Removes a player from the queue
     * Removes a player by cross checking player name
     * Removal done by setting array value to null
     *
     * @param player Player to remove from the queue
     * */
    public synchronized void removeFromQueue(Player player) {
        for (int i = 0; i < queue.length; i++) {
            if (queue[i] != null && queue[i].getName().equals(player.getName())) {
                queue[i] = null;
                break;
            }
        }
    }

    /**
     * Add a player to a specific index in the queue
     * Unlikely you'll ever need to use this.
     *
     * @param i Index to add player to / replace with
     * @param player Player to add / replace with
     * */
    public void queueAtIndex(int i, Player player){
        queue[i] = player;
    }

    public World getWorld() {
        return world;
    }

    /**
     * Set the world for the queue
     *
     * @param world The world for the queue
     * */
    public void setWorld(World world) {
        this.world = world;
    }

    /**
     * Get the max players for the queue
     *
     * @return int The max players for the queue
     * */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Set the max players for the queue
     *
     * @param maxPlayers The max players for the queue
     * */
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    /**
     * Get the associated game
     *
     * @return GameModel The associated game
     * */
    public GameModel getAssociatedGame(){
        return this.associatedGame;
    }

    /**
     * Checks if the queue is full
     * A queue is full when all the spots are taken
     *
     * @return boolean True if the queue is full, false otherwise
     * */
    public boolean isQueueFull(){
        for(int i = 0; i < queue.length; i++){
            if(queue[i] == null){
                return false;
            }
        }
        return true;
    }

    /**
     * Clears the queue of all players
     * */
    public void clearQueue(){
        for(int i = 0; i < queue.length; i++){
            queue[i] = null;
        }
    }

    @Override
    public String toString(){
        return ChatColor.AQUA + "" + ChatColor.BOLD + "QueueModel{" +
                ChatColor.GREEN + "queue=" + ChatColor.WHITE + Arrays.toString(queue) + ChatColor.GREEN + ", " +
                "maxPlayers=" + ChatColor.WHITE + maxPlayers + ChatColor.GREEN + ", " +
                "worldName=" + ChatColor.WHITE + worldName + ChatColor.GREEN + ", " +
                "associatedGame=" + ChatColor.WHITE + associatedGame + ChatColor.GREEN + ", " +
                "world=" + ChatColor.WHITE + world.getName() + ChatColor.GREEN + ", " +
                "startTimer=" + ChatColor.WHITE + startTimer + ChatColor.GREEN + "}";
    }

    public boolean isPlayerInQueue(Player player) {
        for (Player p:
                queue) {
            if(p != null && p.getUniqueId().equals(player.getUniqueId())){
                return true;
            }
        }
        return false;
    }

    public int getNumberOfPlayersInQueue(){
        int count = 0;
        for (Player p:
                queue) {
            if(p != null){
                count++;
            }
        }
        return count;
    }

    public float getPercentageFull(){
        return (float)getNumberOfPlayersInQueue() / (float)maxPlayers;
    }
}
