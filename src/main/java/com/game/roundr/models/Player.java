package com.game.roundr.models;

public class Player extends User {
	private int turn;
	// private String playerStatus;

	public Player(String nickname, int turn) {
		super(nickname);
		this.turn = turn;
		// this.playerStatus = playerStatus;

	}

	// public String getPlayerStatus() {
	// return playerStatus;
	// }

}


