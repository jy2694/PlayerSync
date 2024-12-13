package io.github.jy2694.playersync.event;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerDataLoadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID key;
    private final int loadAmount;

    public PlayerDataLoadEvent(UUID key, int loadAmount) {
        this.key = key;
        this.loadAmount = loadAmount;
    }

    public UUID getKey() {
        return key;
    }

    public int getLoadAmount() {
        return loadAmount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
