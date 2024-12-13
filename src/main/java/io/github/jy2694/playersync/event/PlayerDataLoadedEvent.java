package io.github.jy2694.playersync.event;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerDataLoadedEvent extends Event{

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID key;
    private final int loadedAmount;

    public PlayerDataLoadedEvent(UUID key, int loadedAmount) {
        this.key = key;
        this.loadedAmount = loadedAmount;
    }

    public UUID getKey() {
        return key;
    }

    public int getLoadedAmount() {
        return loadedAmount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
