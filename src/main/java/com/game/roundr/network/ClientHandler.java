package com.game.roundr.network;

import com.game.roundr.models.Player;
import com.game.roundr.models.Message;
import com.game.roundr.models.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {

    private final int maxNumOfPlayers = 6;
    private Socket socket;
    private Server server;
    protected ObjectInputStream input;
    protected ObjectOutputStream output;
    private String clientUsername;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            closeConnection();
        }
    }

    @Override
    public void run() {
        try {
            while (socket.isConnected()) {
                Message inboundMsg = (Message) input.readObject(); // read message

                if (inboundMsg != null) {
                    System.out.println("Server: Received " + inboundMsg.toString());

                    switch (inboundMsg.getMsgType()) { // handle based on messageType
                        case CONNECT -> {
                            Message outboundMsg = new Message(); // reply to be sent 

                            if (server.players.size() == maxNumOfPlayers) { // lobby full
                                outboundMsg.setMsgType(MessageType.CONNECT_FAILED);
                                outboundMsg.setContent("The lobby is full");
                            } else {
                                // TODO update the db
                                
                                // add the new player
                                clientUsername = inboundMsg.getSenderName();
                                server.handlers.add(this);
                                server.players.add(new Player(
                                        inboundMsg.getSenderName(), socket.getInetAddress()));

                                // tell other players that a new player have connected
                                outboundMsg.setMsgType(MessageType.USER_JOINED);
                                outboundMsg.setSenderName(inboundMsg.getSenderName());
                                outboundMsg.setContent(server.getPlayerList());
                                broadcastMessage(outboundMsg);

                                // tell the new player that the connection succeeded
                                outboundMsg.setMsgType(MessageType.CONNECT_OK);
                                outboundMsg.setSenderName(server.hostUsername);
                                outboundMsg.setContent(server.getPlayerList());

                                // TODO: add the message to the chat
                                System.out.println("Chat: " + inboundMsg.getSenderName() + " has joined");
                            }
                            // send back msg to the new player
                            output.writeObject(outboundMsg);
                            break;
                        }
                        case DISCONNECT -> {
                            // TODO: add the message to the chat areas
                            System.out.println("Chat: " + inboundMsg.getSenderName() + " has left");

                            // remove the player from the list
                            for (int i = 0; i < server.players.size(); i++) {
                                if (server.players.get(i).getUsername().equals(inboundMsg.getSenderName())) {
                                    server.players.remove(i);
                                    break;
                                }
                            }
                            
                            // tell other players of the disconnection
                            inboundMsg.setContent(server.getPlayerList());
                            broadcastMessage(inboundMsg);

                            // close socket
                            closeConnection();
                            break;
                        }
                        default -> {
                            System.out.println("Server: Received unknown message type: " + inboundMsg.getMsgType());
                        }
                    }
                }
            }
        } catch (SocketException e) {
            if (e.getMessage().contains("Connection reset")) {
                System.out.println("Server: Client connection reset");
            }
            else if (e.getMessage().contains("Socket closed")) {
                System.out.println("Server: Client socket closed");
            } else {
                e.printStackTrace();
            }
        } catch (IOException | ClassNotFoundException e) { // other errs
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public void broadcastMessage(Message msg) {
        // send to all players except the sender
        for (ClientHandler handler : server.handlers) {
            try {
                if (!handler.clientUsername.equals(clientUsername)) {
                    handler.output.writeObject(msg);
                    handler.output.flush(); // send any buffered ouput bytes
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void closeConnection() {
        server.handlers.remove(this);
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
