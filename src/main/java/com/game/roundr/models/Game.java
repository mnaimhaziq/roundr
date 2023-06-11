package com.game.roundr.models;

public class Game {

    private int gameID;
    private String gameStatus;
    private int maxPlayers;
    private int numOfPlayers;
    private String ipAddress;
    private String hostName;

    public Game(
            int gameID,
            String gameStatus,
            int maxPlayers,
            int numOfPlayers,
            String ipAddress,
            String hostName
    ) {
        this.gameID = gameID;
        this.gameStatus = gameStatus;
        this.maxPlayers = maxPlayers;
        this.numOfPlayers = numOfPlayers;
        this.ipAddress = ipAddress;
        this.hostName = hostName;
    }

    public int getGameID() {
        return gameID;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public int getPlayerLimit() {
        return maxPlayers;
    }

    public int getNumOfPlayers() {
        return numOfPlayers;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getHostName() {
        return hostName;
    }

}
