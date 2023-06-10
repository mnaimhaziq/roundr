package com.game.roundr.networking;

public interface ClientInt {
    public void sendChatMessage(String content);
	public void sendReady(boolean ready);
	public void CloseClient();
}
