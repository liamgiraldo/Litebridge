package me.liamgiraldo.litebridge.models;

import me.liamgiraldo.litebridge.Litebridge;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;

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

    public QueueModel(int maxPlayer, String worldName, GameModel associatedGame){
        this.maxPlayers = maxPlayers;
        queue = new Player[this.maxPlayers];
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
    public void appendToQueue(Player player){
        for(int i = 0; i < queue.length; i++){
            if(queue[i] == null){
                queue[i] = player;
            }
        }
    }

    /**
     * Removes a player from the queue
     * Removes a player by cross checking player name
     * Removal done by setting array value to null
     *
     * @param player Player to remove from the queue
     * */
    public void removeFromQueue(Player player){
        for(int i = 0; i < queue.length; i++){
            if(queue[i].getName().equals(player.getName())){
                queue[i] = null;
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

    public void setWorld(World world) {
        this.world = world;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public boolean isQueueFull(){
        for(int i = 0; i < queue.length; i++){
            if(queue[i] == null){
                return false;
            }
        }
        return true;
    }
}
