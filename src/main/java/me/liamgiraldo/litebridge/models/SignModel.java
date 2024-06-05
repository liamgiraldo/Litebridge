package me.liamgiraldo.litebridge.models;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.material.Sign;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

public class SignModel {
    private GameModel game;
    private ArrayList<Player> queue;
    private Vector<Integer> position;
    private int maxQueueTime;
    private long timeAtWhichQueueTimerStarted;
    //this might want to hold the sign itself but I am not sure how to do that yet
    private Block sign;
    public SignModel(GameModel game, int maxQueueTime, Block sign){
        this.game = game;
        this.queue = new ArrayList<>();
        //idk how it's gonna get this
        this.maxQueueTime = maxQueueTime;
        this.sign = sign;

        this.position = new Vector<Integer>();
        this.position.set(0, this.sign.getX());
        this.position.set(1,this.sign.getY());
        this.position.set(2,this.sign.getZ());
    }
    public void addToQueue(Player player){
        this.queue.add(player);
    }
    public void removeFromQueue(int index){
        this.queue.remove(index);
    }
    private void setTimeAtWhichQueueTimerStarted(long time){
        this.timeAtWhichQueueTimerStarted = time;
    }

    public ArrayList<Player> getQueue() {
        return queue;
    }

    public Vector<Integer> getPosition() {
        return position;
    }

    public int getMaxQueueTime() {
        return maxQueueTime;
    }

    public long getTimeAtWhichQueueTimerStarted() {
        return timeAtWhichQueueTimerStarted;
    }

    public Block getSign() {
        return sign;
    }
}
