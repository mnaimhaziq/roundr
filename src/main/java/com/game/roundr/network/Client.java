package com.game.roundr.network;

import com.game.roundr.game.MainGameAreaController;
import com.game.roundr.models.Player;
import com.game.roundr.models.Message;
import com.game.roundr.models.MessageType;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Client {

    private final ClientListener listener;
    protected ObjectOutputStream output;
    protected ObjectInputStream input;
    protected final String username;
    private final int PORT = 9001;


    public Client(String address, String username, MainGameAreaController mgac) {
        this.username = username;
        this.listener = new ClientListener(address, PORT, this, mgac);
    }

    public void startClient() {
        new Thread(listener).start();
    }

    public void closeClient() {
        Message msg = new Message(
                MessageType.DISCONNECT, username, "");
        this.sendMessage(msg);
    }

    private void sendMessage(Message message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected List<Player> extractUserList(String s) {
        List<Player> list = new ArrayList<>();

        String[] players = s.split(";");

        for (String playerStr : players) {
            String[] playerInfo = playerStr.split(","); // get info
            Player player = new Player(playerInfo[0]);
            player.setIsReady(Boolean.parseBoolean(playerInfo[1]));
            list.add(player);
        }

        return list;
    }

    public void sendEndGameRequest() {
        Message message = new Message(MessageType.END_GAME, username, "");
        sendMessage(message);
    }

    private void handleIncomingMessage(Message message) {
        // Handle different types of messages
//        switch (message.getMsgType()) {
//            case END_GAME:
//                // Handle end game message
//                // Pause the timer and show the popup
//                Platform.runLater(() -> {
//                    mgac.timer.pause();
//                    mgac.showEndGamePopup();
//                });
//                break;
//            // Handle other message types as needed
//        }
    }



}
