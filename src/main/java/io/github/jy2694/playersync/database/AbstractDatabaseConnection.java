package io.github.jy2694.playersync.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import io.github.jy2694.playersync.PlayerSync;
import io.github.jy2694.playersync.api.PlayerSyncAPI;
import io.github.jy2694.playersync.event.PlayerDataLoadEvent;
import io.github.jy2694.playersync.event.PlayerDataLoadedEvent;
import io.github.jy2694.playersync.event.PlayerDataStoreEvent;
import io.github.jy2694.playersync.event.PlayerDataStoredEvent;

public abstract class AbstractDatabaseConnection {
    protected final PlayerSync plugin;
    protected final String host;
    protected final int port;

    public AbstractDatabaseConnection(PlayerSync plugin, String host, int port) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
    }

    public abstract void connect();

    public abstract void disconnect();

    protected abstract <T> T getData(UUID key, Class<T> clazz);

    protected abstract void saveData(Object data);

    public void savePlayerData(UUID key) {
        if(!PlayerSync.getAPI().getStorage().isLoaded(key)) return;
        PlayerSyncAPI api = PlayerSync.getAPI();
        Bukkit.getPluginManager().callEvent(new PlayerDataStoreEvent(key, api.getBinder().getClassList().size()));
        new Thread(() -> {
            List<Thread> threads = new CopyOnWriteArrayList<>();
            for (Class<?> clazz : api.getBinder().getClassList()) {
                Thread thread = new Thread(() -> {
                    Object data = api.getStorage().getData(key, clazz);
                    saveData(data);
                });
                thread.start();
                threads.add(thread);
            }
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                PlayerSync.getAPI().getStorage().removeIsLoaded(key);
                //send release message to other server
                Bukkit.getPluginManager().callEvent(new PlayerDataStoredEvent(key, api.getBinder().getClassList().size()));
            });
        }).start();
    }

    public void loadPlayerData(UUID key) {
        PlayerSyncAPI api = PlayerSync.getAPI();
        Bukkit.getPluginManager().callEvent(new PlayerDataLoadEvent(key, api.getBinder().getClassList().size()));
        PlayerSync.getAPI().getStorage().addIsLoading(key);
        plugin.getVelocityChannel().tryToLoad(key)
            .thenAccept(result -> {
                if(!result) return;
                new Thread(() -> {
                    List<Thread> threads = new CopyOnWriteArrayList<>();
                    for (Class<?> clazz : api.getBinder().getClassList()) {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Object data = getData(key, clazz);
                                api.getStorage().putData(key, clazz, data);
                            }
                        });
                        thread.start();
                        threads.add(thread);
                    }
                    for (Thread thread : threads) {
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        PlayerSync.getAPI().getStorage().removeIsLoading(key);
                        PlayerSync.getAPI().getStorage().addIsLoaded(key);
                        Bukkit.getPluginManager().callEvent(new PlayerDataLoadedEvent(key, api.getBinder().getClassList().size()));
                    });
                }).start();
        });
    }

    protected String serialize(Object data) {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {
            oos.writeObject(data);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected <T> T deserialize(String data, Class<T> clazz) {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        BukkitObjectInputStream ois = new BukkitObjectInputStream(bais)) {
            return clazz.cast(ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
