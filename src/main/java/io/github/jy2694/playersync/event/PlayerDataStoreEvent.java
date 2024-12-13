package io.github.jy2694.playersync.event;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerDataStoreEvent extends Event{

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID key;
    private final int storeAmount;

    public PlayerDataStoreEvent(UUID key, int storeAmount) {
        this.key = key;
        this.storeAmount = storeAmount;
    }

    public UUID getKey() {
        return key;
    }

    public int getStoreAmount() {
        return storeAmount;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
