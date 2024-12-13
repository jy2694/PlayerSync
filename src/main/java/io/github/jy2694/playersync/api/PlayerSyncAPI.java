package io.github.jy2694.playersync.api;

import java.util.UUID;

import io.github.jy2694.playersync.PlayerSync;
import io.github.jy2694.playersync.exception.NotBindedClassException;
import io.github.jy2694.playersync.registry.ClassBinder;
import io.github.jy2694.playersync.registry.PlayerDataStorage;

public class PlayerSyncAPI {
    
    private final PlayerSync plugin;
    private final ClassBinder binder;
    private final PlayerDataStorage storage;

    public PlayerSyncAPI(PlayerSync plugin) {
        this.plugin = plugin;
        binder = new ClassBinder();
        storage = new PlayerDataStorage();
    }

    public ClassBinder getBinder() {
        return binder;
    }

    public PlayerDataStorage getStorage() {
        return storage;
    }

    public <T> T get(Class<T> clazz, UUID uuid) throws NotBindedClassException{
        if(!binder.getClassList().contains(clazz)) {
            throw new NotBindedClassException("Class " + clazz.getName() + " is not binded");
        }
        Object data = storage.getData(uuid, clazz);
        if(data == null) return null;
        return clazz.cast(data);
    }
}
