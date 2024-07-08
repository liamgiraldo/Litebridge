package me.liamgiraldo.litebridge.events;

import me.liamgiraldo.litebridge.models.GameModel;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameEndEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private GameModel game;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public GameModel getGame() {
        return game;
    }

    public GameEndEvent(GameModel game) {
        this.game = game;
    }
}
