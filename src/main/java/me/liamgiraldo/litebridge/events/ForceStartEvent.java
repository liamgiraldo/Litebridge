package me.liamgiraldo.litebridge.events;

import me.liamgiraldo.litebridge.models.QueueModel;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ForceStartEvent extends Event {
    private QueueModel queue;
    private static final HandlerList HANDLERS = new HandlerList();
    public ForceStartEvent(QueueModel queue) {
        this.queue = queue;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public QueueModel getQueue() {
        return queue;
    }
}
