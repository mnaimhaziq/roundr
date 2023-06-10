package com.game.roundr.models;

import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L; // for verification

    private MessageType msgType;
    private String senderName;
    private String timestamp;
    private String content;

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

    public void setContent(String content) {
        this.content = content;
    }

    public String toString() {
        return this.senderName + "(" + this.msgType.toString() + "): " + this.content;
    }
}
