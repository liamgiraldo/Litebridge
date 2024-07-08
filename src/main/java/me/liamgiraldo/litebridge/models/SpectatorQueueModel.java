package me.liamgiraldo.litebridge.models;

import me.liamgiraldo.litebridge.interfaces.SpectatorAction;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class SpectatorQueueModel {
    ArrayList<Player> spectators;
    World world;
    QueueModel associatedQueue;

    public GameModel getAssociatedGame() {
        return associatedGame;
    }

    public void setAssociatedGame(GameModel associatedGame) {
        this.associatedGame = associatedGame;
    }

    public QueueModel getAssociatedQueue() {
        return associatedQueue;
    }

    public void setAssociatedQueue(QueueModel associatedQueue) {
        this.associatedQueue = associatedQueue;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public ArrayList<Player> getSpectators() {
        return spectators;
    }

    public void setSpectators(ArrayList<Player> spectators) {
        this.spectators = spectators;
    }

    GameModel associatedGame;
    public SpectatorQueueModel(World world, QueueModel queueModel, GameModel gameModel) {
        spectators = new ArrayList<>();
        this.world = world;
        this.associatedQueue = queueModel;
        this.associatedGame = gameModel;
    }

    public void addSpectator(Player player) {
        spectators.add(player);
    }

    public void removeSpectator(Player player) {
        spectators.remove(player);
    }

    public void clearSpectators() {
        spectators.clear();
    }

    public void performActionOnEachSpectator(SpectatorAction action) {
        for (Player player : spectators) {
            action.perform(player);
        }
    }

    public void performActionOnSingleSpectator(Player player, SpectatorAction action) {
        action.perform(player);
    }
}
