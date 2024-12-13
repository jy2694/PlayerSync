package io.github.jy2694.playersync.channel;

import java.util.UUID;

public class Message {

    public enum MessageType {
        TRY_TO_LOAD,
        CAN_LOAD,
        CANT_LOAD,
        RELEASE_RESOURCE;
    }

    private final MessageType type;
    private final UUID messageId;
    private final UUID playerId;
    private final Boolean response;
    private final String sender;
    private final String receiver;

    public Message(MessageType type, UUID messageId, UUID playerId, String sender, String receiver){
        this(type, messageId, playerId, null, sender, receiver);
    }

    public Message(MessageType type, UUID messageId, UUID playerId, Boolean response, String sender, String receiver){
        this.type = type;
        this.messageId = messageId;
        this.playerId = playerId;
        this.response = response;
        this.sender = sender;
        this.receiver = receiver;
    }

    public MessageType getType(){
        return this.type;
    }

    public UUID getMessageId(){
        return this.messageId;
    }

    public UUID getPlayerId(){
        return this.playerId;
    }

    public Boolean getResponse(){
        return this.response;
    }

    public String getSender(){
        return this.sender;
    }

    public String getReceiver(){
        return this.receiver;
    }

    public static Message fromString(String message){
        String[] parts = message.split(":");
        if(parts.length != 4){
            return null;
        }
        return new Message (MessageType.valueOf(parts[0]), UUID.fromString(parts[1]), UUID.fromString(parts[2]), parts[3].equals("null") ? null : Boolean.parseBoolean(parts[3]), parts[4], parts[5]);
    }

    public String toString(){
        return this.type.toString() + ":" + this.messageId.toString() + ":" + this.playerId.toString() + ":" + (this.response == null ? "null" : this.response.toString()) + ":" + this.sender + ":" + this.receiver;
    }
}