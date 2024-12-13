package io.github.jy2694.playersync.channel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import io.github.jy2694.playersync.PlayerSync;
import io.github.jy2694.playersync.registry.PlayerDataStorage;

public class VelocityChannel {

    private final PlayerSync plugin;
    private final List<UUID> messageQueue = new CopyOnWriteArrayList<>();
    private final Map<UUID, Message> messageMap = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitRunnable> timeoutMap = new ConcurrentHashMap<>();
    private final Map<UUID, Object> responseValue = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> responseCounter = new ConcurrentHashMap<>();

    public VelocityChannel(PlayerSync plugin) {
        this.plugin = plugin;
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, "playersync", new VelocityMessageReceiver(this));
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "playersync");
    }

    public PlayerSync getPlugin() {
        return plugin;
    }

    public void clearPlayerMessage(UUID playerUuid){
        messageMap.entrySet().stream().filter(entry -> entry.getValue().getPlayerId().equals(playerUuid)).map(Map.Entry::getValue).forEach(msg -> {
            messageMap.remove(msg.getMessageId());
            messageQueue.remove(msg.getMessageId());
            responseCounter.remove(msg.getMessageId());
            responseValue.put(msg.getMessageId(), false);
            BukkitRunnable runnable = timeoutMap.get(msg.getMessageId());
            if(runnable != null) runnable.cancel();
            timeoutMap.remove(msg.getMessageId());
        });
    }

    private void sendMessage(Message msg) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Forward");
            out.writeUTF("ALL");
            out.writeUTF("playersync");
            byte[] data = msg.toString().getBytes();
            out.writeShort(data.length);
            out.write(data);

            Player p = Bukkit.getOnlinePlayers().stream().findAny().orElse(null);
            if(p == null) return;

            p.sendPluginMessage(plugin, "playersync", b.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendReleaseMessage(UUID pid) {
        Message msg = new Message(Message.MessageType.RELEASE_RESOURCE, UUID.randomUUID(), pid,
                this.plugin.getConfiguration().getServerName(), "ALL");
        sendMessage(msg);
    }

    public CompletableFuture<Boolean> tryToLoad(UUID pid) {
        if (!plugin.getConfiguration().isVelocityEnable())
            return CompletableFuture.completedFuture(true);
        return CompletableFuture.supplyAsync(() -> {
            Message msg = new Message(Message.MessageType.TRY_TO_LOAD, UUID.randomUUID(), pid,
                    this.plugin.getConfiguration().getServerName(), "ALL");
            messageQueue.add(msg.getMessageId());
            messageMap.put(msg.getMessageId(), msg);
            responseCounter.put(msg.getMessageId(), plugin.getConfiguration().getTotalServerCount());
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    messageQueue.remove(msg.getMessageId());
                    messageMap.remove(msg.getMessageId());
                    responseCounter.remove(msg.getMessageId());
                    responseValue.put(msg.getMessageId(), true);
                    timeoutMap.remove(msg.getMessageId());
                }
            };
            timeoutMap.put(msg.getMessageId(), runnable);
            runnable.runTaskLater(plugin, plugin.getConfiguration().getRequestTimeoutTicks());
            sendMessage(msg);
            while (!responseValue.containsKey(msg.getMessageId()))
                Thread.onSpinWait();
            return (Boolean) responseValue.get(msg.getMessageId());
        });
    }

    public class VelocityMessageReceiver implements PluginMessageListener {

        private final VelocityChannel channel;

        public VelocityMessageReceiver(VelocityChannel channel) {
            this.channel = channel;
        }

        @Override
        public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
            try {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
                String subchannel = in.readUTF();
                if (!subchannel.equals("playersync"))
                    return;
                short len = in.readShort();
                byte[] data = new byte[len];
                in.readFully(data);
                String s = new String(data);
                Message msg = Message.fromString(s);
                if (msg == null)
                    return;
                PlayerDataStorage storage = PlayerSync.getAPI().getStorage();
                if (!msg.getReceiver().equalsIgnoreCase("ALL")
                        && !msg.getReceiver().equals(this.channel.getPlugin().getConfiguration().getServerName()))
                    return;
                if (msg.getType() == Message.MessageType.TRY_TO_LOAD) {
                    if (storage.isLoaded(msg.getPlayerId()) || storage.isLoading(msg.getPlayerId())) {
                        this.channel
                                .sendMessage(
                                        new Message(Message.MessageType.CANT_LOAD, msg.getMessageId(),
                                                msg.getPlayerId(),
                                                false, this.channel.getPlugin().getConfiguration().getServerName(),
                                                msg.getSender()));
                    } else {
                        this.channel
                                .sendMessage(
                                        new Message(Message.MessageType.CAN_LOAD, msg.getMessageId(), msg.getPlayerId(),
                                                true, this.channel.getPlugin().getConfiguration().getServerName(),
                                                msg.getSender()));
                    }
                } else if (msg.getType() == Message.MessageType.CAN_LOAD) {
                    if (!responseCounter.containsKey(msg.getMessageId()))
                        return;
                    int nextCount = responseCounter.get(msg.getMessageId()) - 1;
                    if (nextCount == 0) {
                        responseValue.put(msg.getMessageId(), true);
                        responseCounter.remove(msg.getMessageId());
                        BukkitRunnable runnable = timeoutMap.get(msg.getMessageId());
                        if (runnable != null)
                            runnable.cancel();
                        timeoutMap.remove(msg.getMessageId());
                        // 모든 서버에서 데이터 로드 승인
                    } else {
                        responseCounter.put(msg.getMessageId(), nextCount);
                        // 일부 서버에서 데이터 로드 승인, 추가 대기
                    }
                } else if (msg.getType() == Message.MessageType.CANT_LOAD) {
                    if (!responseCounter.containsKey(msg.getMessageId()))
                        return;
                    responseCounter.remove(msg.getMessageId());
                    responseValue.remove(msg.getMessageId());
                    BukkitRunnable runnable = timeoutMap.get(msg.getMessageId());
                    if (runnable != null)
                        runnable.cancel();
                    timeoutMap.remove(msg.getMessageId());
                    // 일부 서버에서 데이터 로드 거부, 대기 타이머 취소 및 RELEASE 대기 상태
                } else if (msg.getType() == Message.MessageType.RELEASE_RESOURCE) {
                    // 특정 서버에서 데이터 사용을 마침
                    UUID playerUuid = msg.getPlayerId();
                    // 점유 해제된 데이터를 요구 중이라면 요구 중인 메시지 취소 및 점유 가능 상태 변경
                    List<Message> waitingTargetMessage = messageMap.entrySet().stream()
                            .filter(entry -> entry.getValue().getPlayerId().equals(playerUuid)).map(Map.Entry::getValue)
                            .toList();
                    for (Message targetMessage : waitingTargetMessage) {
                        messageMap.remove(targetMessage.getMessageId());
                        messageQueue.remove(targetMessage.getMessageId());
                        BukkitRunnable runnable = timeoutMap.get(msg.getMessageId());
                        if (runnable != null)
                            runnable.cancel();
                        timeoutMap.remove(msg.getMessageId());
                        responseCounter.remove(msg.getMessageId());
                        responseValue.put(msg.getMessageId(), true);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
