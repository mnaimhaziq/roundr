package com.game.roundr.networking;

public interface ServerInt {
    public void sendChatMessage(String content);
	public boolean checkCanStartGame();
	public void CloseServer();
}
