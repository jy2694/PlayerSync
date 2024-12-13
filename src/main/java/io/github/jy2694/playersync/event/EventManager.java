package io.github.jy2694.playersync.event;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.github.jy2694.playersync.PlayerSync;

public class EventManager implements Listener {
    private final PlayerSync plugin;

    public EventManager(PlayerSync plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        plugin.getDatabase().loadPlayerData(playerUuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        plugin.getVelocityChannel().clearPlayerMessage(playerUuid);
        plugin.getDatabase().savePlayerData(playerUuid);
    }

    @EventHandler
    public void onPlayerDataLoad(PlayerDataLoadEvent event){
        Player player = Bukkit.getPlayer(event.getKey());
        if(player == null) return;
        //TODO - 플레이어 데이터 로드 시작 이벤트 발생
    }

    @EventHandler
    public void onPlayerDataLoaded(PlayerDataLoadedEvent event){
        Player player = Bukkit.getPlayer(event.getKey());
        if(player == null) return;
        plugin.getVelocityChannel().clearPlayerMessage(event.getKey());
        //TODO - 플레이어 데이터 로드 완료 이벤트 발생
    }
}
