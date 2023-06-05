package com.game.roundr.models;

public class Game {

    private int gameID;
    private String gameStatus;
    private int numOfRounds;
    private int turnTimeLimit;
    private int wordLength;
    private int playerLimit;
    private int numOfPlayers;
    private String ipAddress;
    private String hostName;

    public Game(
            int gameID,
            String gameStatus,
            int numOfRounds,
            int turnTimeLimit,
            int wordLength,
            int playerLimit,
            int numOfPlayers,
            String ipAddress,
            String hostName
    ) {
        this.gameID = gameID;
        this.gameStatus = gameStatus;
        this.numOfRounds = numOfRounds;
        this.turnTimeLimit = turnTimeLimit;
        this.wordLength = wordLength;
        this.playerLimit = playerLimit;
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

    public int getNumOfRounds() {
        return numOfRounds;
    }

    public int getTurnTimeLimit() {
        return turnTimeLimit;
    }

    public int getWordLength() {
        return wordLength;
    }

    public int getPlayerLimit() {
        return playerLimit;
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
