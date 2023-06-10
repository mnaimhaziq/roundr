package com.game.roundr.models;

import java.net.InetAddress;

public class Player {

    private String username;
    private boolean isReady;
    private InetAddress address;

    public Player(String username) {
        this.username = username;
        this.isReady = false;
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

}


