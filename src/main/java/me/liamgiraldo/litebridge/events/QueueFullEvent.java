package me.liamgiraldo.litebridge.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import me.liamgiraldo.litebridge.models.QueueModel;

public class QueueFullEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final QueueModel queue;

    public QueueFullEvent(QueueModel queue) {
        this.queue = queue;
    }

    public QueueModel getQueue() {
        return queue;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}