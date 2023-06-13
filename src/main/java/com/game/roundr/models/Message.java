package com.game.roundr.models;

import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.Map;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L; // for verification

    private MessageType msgType;
    private String senderName;
    private String timestamp;
    private String content;
    private Map<String, Integer> playerScore;

    public Message() {
    }

    public Message(String type, String senderName, String content) {
        this.msgType = MessageType.valueOf(type);
        this.senderName = senderName;
        this.content = content;
    }

    public Message(MessageType type, String senderName, String content) {
        this.msgType = type;
        this.senderName = senderName;
        this.content = content;
    }

    public Message(MessageType type, String senderName, Map<String, Integer> playerScore) {
        this.msgType = type;
        this.senderName = senderName;
        this.playerScore = playerScore;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public void setMsgType(MessageType msgType) {
        this.msgType = msgType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }
    public Map<String, Integer> getPLayerScore() {
        return playerScore;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPlayerScore(Map<String, Integer> playerScore) {
        this.playerScore = playerScore;
    }

    public String toString() {
        return this.senderName + "(" + this.msgType.toString() + "): " + this.content;
    }
}
