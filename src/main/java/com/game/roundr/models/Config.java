package com.game.roundr.models;

public class Config {
    public final int numOfRounds;
    public final int turnTimeLimit;
    public final int wordLength;
    public final int maxPlayers;

    public Config(int numOfRounds, int turnTimeLimit, int wordLength, int maxPlayers) {
        this.numOfRounds = numOfRounds;
        this.turnTimeLimit = turnTimeLimit;
        this.wordLength = wordLength;
        this.maxPlayers = maxPlayers;
    }
}
