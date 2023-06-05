package com.game.roundr.network;

import com.game.roundr.App;
import com.game.roundr.models.Message;
import com.game.roundr.models.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

public class ClientListener implements Runnable {

    private Socket socket;
    private final String address;
    private final Client client;
    private final int port;

    public ClientListener(String address, int port, Client client) {
        this.address = address;
        this.client = client;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            client.output = new ObjectOutputStream(socket.getOutputStream());
            client.input = new ObjectInputStream(socket.getInputStream());
            System.out.println("Client: Running. Username: " + client.username);

            // send CONNECT message
            Message msg = new Message(MessageType.CONNECT, client.username, "");
            client.output.writeObject(msg);

            while (this.socket.isConnected()) {
                Message inboundMsg = (Message) client.input.readObject();

                if (inboundMsg != null) {
                    System.out.println("Client: Received " + inboundMsg.toString());

                    switch (inboundMsg.getMsgType()) { // handle according to m.type
                        case CONNECT_FAILED -> {
                            System.out.println("Client: Connection failed");
                            break;
                        }
                        case CONNECT_OK -> {
                            App.setScene("lobby/GameLobby");
                            System.out.println("Client: Connected to server at -" + address);
                            System.out.println("List: " + client.extractUserList(inboundMsg.getContent()).size());

                            // TODO: add the message to the chat
                            System.out.println("Chat: " + client.username + " has joined");
                            break;
                        }
                        case USER_JOINED -> {
                            // TODO: add the message to the chat
                            System.out.println("Chat: " + inboundMsg.getSenderName() + " has joined");
                            break;
                        }
                        case DISCONNECT -> {
                            if (inboundMsg.getSenderName().equals(client.username)) { // the server's closed
                                // switch view
                                App.setScene("MainMenu");

                                // show alert
                                System.out.println("Disconnected from server. Message: " + inboundMsg.getContent());
                                App.client = null;
                            } else { // a player have disconnected
                                // TODO: add message to the chat
                                System.out.println("Chat: " + inboundMsg.getSenderName() + " has left");
                            }
                            break;
                        }
                        default -> {
                            System.out.println("Client: Received unknown message type: " + inboundMsg.toString());
                        }
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Socket exception");
            if (e instanceof ConnectException) {
                System.out.println("Connection failed " + e.getMessage());
            } else if (e.getMessage().equals("Connection reset")) {
                System.out.println("Connection closed");
            }
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Connection closed");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }    }
}
