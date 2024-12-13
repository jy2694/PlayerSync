package io.github.jy2694.playersync.registry;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerDataStorage {
    private final List<UUID> isLoading = new CopyOnWriteArrayList<>();
    private final List<UUID> isLoaded = new CopyOnWriteArrayList<>();
    private final Map<UUID, Map<Class<?>, Object>> playerDataMap = new ConcurrentHashMap<>();

    public void addIsLoading(UUID uuid){
        isLoading.add(uuid);
    }

    public void removeIsLoading(UUID uuid){
        isLoading.remove(uuid);
    }

    public boolean isLoading(UUID uuid){
        return isLoading.contains(uuid);
    }

    public void addIsLoaded(UUID uuid){
        isLoaded.add(uuid);
    }

    public void removeIsLoaded(UUID uuid){
        isLoaded.remove(uuid);
    }

    public boolean isLoaded(UUID uuid){
        return isLoaded.contains(uuid);
    }

    public void putData(UUID key, Class<?> clazz, Object data){
        Map<Class<?>, Object> dataMap = playerDataMap.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        dataMap.put(clazz, data);
    }

    public void removeData(UUID key, Class<?> clazz){
        Map<Class<?>, Object> dataMap = playerDataMap.get(key);
        if(dataMap == null) return;
        dataMap.remove(clazz);
    }

    public <T> T getData(UUID key, Class<T> clazz){
        Map<Class<?>, Object> dataMap = playerDataMap.get(key);
        if(dataMap == null) return null;
        return clazz.cast(dataMap.get(clazz));
    }
}
