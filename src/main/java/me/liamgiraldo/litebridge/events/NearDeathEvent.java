package me.liamgiraldo.litebridge.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class NearDeathEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;

    public NearDeathEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
