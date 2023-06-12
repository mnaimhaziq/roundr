package com.game.roundr.network;

import com.game.roundr.App;
import com.game.roundr.models.Player;
import com.game.roundr.models.Message;
import com.game.roundr.models.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;

public class Client {

    public final ClientListener listener;
    protected ObjectOutputStream output;
    protected ObjectInputStream input;
    protected final String username;
    private final int PORT = 9001;

    public Client(String address, String username) {
        this.username = username;
        this.listener = new ClientListener(address, PORT, this);
    }

    public void startClient() {
        new Thread(listener).start();
    }

    public void closeClient() {
        App.glc.clearPlayers();
        Message msg = new Message(
                MessageType.DISCONNECT, username, "");
        this.sendMessage(msg);
    }

    public ArrayList<Player> extractPlayerList(String s) {
        ArrayList<Player> list = new ArrayList<>();

        String[] players = s.split(";");

        for (String playerStr : players) {
            String[] playerInfo = playerStr.split(",");
            Player player = new Player(playerInfo[0], playerInfo[1]);
            player.setIsReady(Boolean.parseBoolean(playerInfo[2]));
            list.add(player);
        }

        return list;
    }

    // utility to send message to the server
    protected void sendMessage(Message msg) {
        try {
            output.writeObject(msg);
            output.flush();
        } catch (IOException e) {
            System.out.println("Server: Failed to send message to the server");
        }
    }

    public void sendReady(String ready) {
        Message msg = new Message(MessageType.READY, App.username, ready);

        // update the player list
        App.glc.updatePlayer(msg);
        sendMessage(msg);
    }

    public void sendChatMessage(String content) {
        Message message = new Message(MessageType.CHAT, App.username, content);
        sendMessage(message);
    }

    public void sendEndGameRequest() {
        Message message = new Message(MessageType.END_GAME, username, "");
        sendMessage(message);
    }

    public void sendShiftedTurn(String turn) {
        Message message = new Message(MessageType.TURN, App.username, turn);
        sendMessage(message);
    }

    public void sendPlayerScore(Map<String, Integer> scores) {
        Message msg = new Message(MessageType.PLAYER_SCORE, App.username, scores);
        sendMessage(msg);
    }

    public void sendWordMessage(String content) {
        Message msg = new Message(MessageType.RANDOM_WORD, App.username, content);
        sendMessage(msg);
    }
}
