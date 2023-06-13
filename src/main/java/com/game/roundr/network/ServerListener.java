package com.game.roundr.network;

import com.game.roundr.App;
import com.game.roundr.models.Message;
import com.game.roundr.models.MessageType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;


public class ServerListener implements Runnable {

    private final Server server;
    private final ServerSocket serverSocket;
    private ArrayList<ObjectOutputStream> writers;
    public ServerListener(int port, Server server) throws IOException {
        this.server = server;
        this.serverSocket = new ServerSocket(port);
        System.out.println("Server: Listening for connections on port " + port);
    }

    @Override
    public void run() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Server: A player is connected");
                // create a new thread to handle the player connection
                new Thread(new ClientHandler(socket, server)).start();
            }
        } catch (SocketException e) {
            System.out.println("Server: Server socket closed");
        } catch (IOException e) {
            System.out.println("Server: Unable to accept the new connection");
        } finally {
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessageToClients(Message message) {
        // Send the message to all connected clients
        for (ClientHandler handler : server.handlers) {
            try {
                handler.output.writeObject(message);
                handler.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendChatMessage(String content)
    {
        Message msg = new Message(MessageType.CHAT, App.username, content);

        // send the chat message to everyone
        this.sendChatMessage(msg);
    }

    private void sendChatMessage(Message message)
    {
        // send the message to each user except the server
        for (ClientHandler handler : server.handlers) {
            try {
                handler.output.writeObject(message);
                handler.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendWordMessage(String content)
    {
        Message msg = new Message(MessageType.RANDOM_WORD, App.username, content);

        // send the chat message to everyone
        this.sendWordMessage(msg);
    }

    private void sendWordMessage(Message message)
    {
        // send the message to each user except the server
        for (ClientHandler handler : server.handlers) {
            try {
                handler.output.writeObject(message);
                handler.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPlayerScore(Map<String, Integer> playerScore)
    {
        Message msg = new Message(MessageType.PLAYER_SCORE, App.username, playerScore);

        // send the chat message to everyone
        this.sendPlayerScore(msg);
    }

    private void sendPlayerScore(Message message)
    {
        // send the message to each user except the server
        for (ClientHandler handler : server.handlers) {
            try {
                handler.output.writeObject(message);
                handler.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendShiftedTurn(String turn)
    {
        Message msg = new Message(MessageType.TURN, App.username, turn);

        // send the chat message to everyone
        this.sendShiftedTurn(msg);
    }

    private void sendShiftedTurn(Message message)
    {
        // send the message to each user except the server
        for (ClientHandler handler : server.handlers) {
            try {
                handler.output.writeObject(message);
                handler.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPlayerState(List<Object> playerState)
    {
        Message msg = new Message(MessageType.PLAYER_STATE, App.username, playerState);

        // send the chat message to everyone
        this.sendPlayerState(msg);
    }

    private void sendPlayerState(Message message)
    {
        // send the message to each user except the server
        for (ClientHandler handler : server.handlers) {
            try {
                handler.output.writeObject(message);
                handler.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendEndGameRequest() {
        Message message = new Message(MessageType.END_GAME,
                App.username, "");
        this.sendEndGameRequest(message);
    }

    private void sendEndGameRequest(Message message)
    {
        // send the message to each user except the server
        for (ClientHandler handler : server.handlers) {
            try {
                handler.output.writeObject(message);
                handler.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
