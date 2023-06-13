package com.game.roundr.network;

import com.game.roundr.App;
import com.game.roundr.models.Player;

import javafx.scene.layout.VBox;

import com.game.roundr.models.Message;
import com.game.roundr.models.MessageType;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.util.ArrayList;

public class Server {

    private static final int PORT = 9001;
    protected final String hostUsername;
    private ServerListener listener;
    protected ArrayList<Player> players = new ArrayList<>();
    protected ArrayList<ClientHandler> handlers = new ArrayList<>();
    protected ObjectOutputStream output;

    public Server(String username) {
        this.hostUsername = username;
        this.players.add(new Player(username)); // adds host player to list
    }

    public void startServer() throws IOException {
        try {
            listener = new ServerListener(PORT, this);
            new Thread(listener).start(); // network thread
            
            // change view
            App.setScene("lobby/GameLobby");
        } catch (BindException e) {
            System.out.println("Server: The port and address is in use");
        } catch (IOException e) {
            System.out.println("Server: Failed to create server socket");
        }
    }

    public void closeServer() {
        Message msg = new Message(MessageType.DISCONNECT,
                hostUsername, "Server closed");

        // send server shutdown message to all connected players
        while (!handlers.isEmpty()) {
            try {
                handlers.get(0).output.writeObject(msg);
                handlers.get(0).closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // close server socket
        listener.closeServerSocket();
        App.server = null;
    }

    public String getPlayerList() {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            list.append(player.getUsername())
                    .append(",")
                    .append(player.isReady());
            if (i != players.size() - 1) {
                list.append(";");
            }
        }
        return list.toString();
    }

    public void sendEndGameRequest() {
        Message message = new Message(MessageType.END_GAME, hostUsername, "");
        sendMessage(message);
    }

    private void sendMessage(Message message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
