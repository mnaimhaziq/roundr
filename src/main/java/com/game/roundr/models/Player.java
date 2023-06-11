package com.game.roundr.models;

import java.net.InetAddress;

public class Player {

    private String username;
    private boolean isReady;
    private String color;
    private InetAddress address;

    public Player(String username) {
        this.username = username;
        this.isReady = false;
    }

    public Player(String username, String color) {
        this.username = username;
        this.color = color;
        this.isReady = false;
    }
    
    public Player(String username, String color, boolean isReady) {
        this.username = username;
        this.color = color;
        this.isReady = isReady;
    }
    
    public Player(String username, InetAddress address) {
        this.username = username;
        this.address = address;
        this.isReady = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setIsReady(boolean isReady) {
        this.isReady = isReady;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

}
