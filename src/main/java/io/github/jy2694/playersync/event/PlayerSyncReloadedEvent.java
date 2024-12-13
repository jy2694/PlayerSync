package io.github.jy2694.playersync.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerSyncReloadedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public PlayerSyncReloadedEvent() {}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
