package com.game.roundr.models;

public class Game {

    private int gameID;
    private String gameStatus;
    private int numOfRounds;
    private int turnTimeLimit;
    private int wordLength;
    private int playerLimit;
    private int numOfPlayers;

    public Game(int gameID, String gameStatus, int numOfRounds, int turnTimeLimit, int wordLength, int playerLimit, int numOfPlayers) {
        this.gameID = gameID;
        this.gameStatus = gameStatus;
        this.numOfRounds = numOfRounds;
        this.turnTimeLimit = turnTimeLimit;
        this.wordLength = wordLength;
        this.playerLimit = playerLimit;
        this.numOfPlayers = numOfPlayers;
    }

    public int getGameID() {
        return gameID;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public int getPlayerLimit() {
        return playerLimit;
    }

    public int getNumOfPlayers() {
        return numOfPlayers;
    }

}
