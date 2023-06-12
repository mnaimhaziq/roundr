package com.game.roundr.network;

import com.game.roundr.App;
import com.game.roundr.DatabaseConnection;
import com.game.roundr.models.Message;
import com.game.roundr.models.MessageType;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClientHandler implements Runnable {

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
                Message inboundMsg = (Message) input.readObject();

                if (inboundMsg != null) {
                    System.out.println("Server: Received " + inboundMsg.toString());

                    switch (inboundMsg.getMsgType()) { // handle in relation to type
                        case CONNECT -> {
                            Message outboundMsg = new Message(); // reply to be sent 

                            if (App.glc.getPlayerSize() == server.config.maxPlayers) {
                                outboundMsg.setMsgType(MessageType.CONNECT_FAILED); // decline player
                                outboundMsg.setContent("The lobby is full");
                                System.out.println("Server: Connection failed");

                                // send the reply msg to the client
                                output.writeObject(outboundMsg);
                            } else {
                                // create random color for the player
                                String color = App.getHexColorCode();

                                // update database tables
                                try {
                                    // create player_game entry in the db
                                    Connection conn = new DatabaseConnection().getConnection();
                                    PreparedStatement stmt = conn.prepareStatement("INSERT INTO player_game"
                                            + "(game_id, player_id, is_host, player_color, final_score) "
                                            + "SELECT game.game_id, player.player_id, 0, ?, 0 FROM game "
                                            + "JOIN player WHERE game.game_id = ? AND player.username = ?");
                                    stmt.setString(1, color);
                                    stmt.setInt(2, server.gameId);
                                    stmt.setString(3, inboundMsg.getSenderName());
                                    stmt.executeUpdate();

                                    // increase number of players in-game
                                    stmt = conn.prepareStatement("UPDATE game "
                                            + "SET player_count = player_count + 1 WHERE `game_id` = ?;");
                                    stmt.setInt(1, server.gameId);
                                    stmt.executeUpdate();

                                    // close db resources
                                    conn.close();
                                    stmt.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                // assign a handler to the player and add to the server lists
                                clientUsername = inboundMsg.getSenderName();
                                App.glc.addPlayer(clientUsername, color);
                                server.handlers.add(this);

                                // inform the current players that a new player joined
                                outboundMsg.setMsgType(MessageType.USER_JOINED);
                                outboundMsg.setSenderName(inboundMsg.getSenderName());
                                outboundMsg.setContent(server.getPlayerList());
                                broadcastMessage(outboundMsg);

                                // inform the new player that connection is confirmed
                                outboundMsg.setMsgType(MessageType.CONNECT_OK);
                                outboundMsg.setSenderName(App.username);
                                outboundMsg.setContent(server.getPlayerList()
                                        .concat(";" + server.gameId + ";" + App.username));

                                // send the reply msg to the client
                                output.writeObject(outboundMsg);

                                // add the message to the chat
                                App.glc.addToTextArea("Server: " + inboundMsg.getSenderName() + " has joined");
                            }
                            break;
                        }
                        case DISCONNECT -> {
                            // add the message to the chat
                            App.glc.addToTextArea("Server: " + inboundMsg.getSenderName() + " has left");

                            // remove the player from the server list
                            App.glc.removePlayer(inboundMsg);

                            // remove the player_game row from the db
                            try {
                                Connection conn = new DatabaseConnection().getConnection();
                                PreparedStatement stmt = conn.prepareStatement("DELETE "
                                        + "FROM `player_game` WHERE player_id = (SELECT player_id FROM "
                                        + "player WHERE username = ?) AND `game_id` = ?;");
                                stmt.setString(1, inboundMsg.getSenderName());
                                stmt.setInt(2, server.gameId);
                                stmt.executeUpdate();

                                // decrease number of players in-game
                                stmt = conn.prepareStatement("UPDATE game "
                                        + "SET `player_count` = player_count - 1 WHERE `game_id` = ?;");
                                stmt.setInt(1, server.gameId);
                                stmt.executeUpdate();

                                // close db resources
                                conn.close();
                                stmt.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            // inform other current players of the disconnection
                            inboundMsg.setContent(server.getPlayerList());
                            broadcastMessage(inboundMsg);

                            // close socket
                            closeConnection();
                            break;
                        }
                        case READY -> {
                            // update database
                            server.updateReady(inboundMsg);

                            // update player inside server list
                            App.glc.updatePlayer(inboundMsg);

                            // forward message to other players
                            broadcastMessage(inboundMsg);

                            // if all ready, then start
                            App.glc.startGame();

                            break;
                        }
                        case CHAT -> {
                            if (App.mgac != null) {
                                App.mgac.addToTextArea(inboundMsg);

                            } else if (App.glc != null) {
                                App.glc.addToTextArea(inboundMsg);
                            }
                            // forward the chat message
                            broadcastMessage(inboundMsg);
                            break;
                        }
                        case END_GAME -> {
                            if (App.mgac != null) {
                                App.mgac.passedEndGamePopup(inboundMsg);
                                System.out.println("Client Handler: not null");
                            } else {
                                System.out.println("Client Handler: null");
                            }
                            broadcastMessage(inboundMsg);
                            break;
                        }
                        case RANDOM_WORD -> {
                            if (App.mgac != null) {
                                App.mgac.generateWordPass(inboundMsg);
                                System.out.println("Client Handler: not null " + inboundMsg.getContent());
                            } else {
                                System.out.println("Client Handler: null " + inboundMsg.getContent());
                            }
                            broadcastMessage(inboundMsg);
                            break;
                        }
                        case PLAYER_SCORE -> {
                            if (App.mgac != null) {
                                App.mgac.passedScorePass(inboundMsg);
                                System.out.println("Client Handler PlayerScore: not null ");
                            } else {
                                System.out.println("Client Handler PlayerScore: null ");
                            }
                            broadcastMessage(inboundMsg);
                            break;
                        }
                        case TURN -> {
                            if (App.mgac != null) {
                                App.mgac.passedShiftedTurn(inboundMsg);
                                System.out.println("Client Handler Turn: not null ");
                            } else {
                                System.out.println("Client Handler Turn: null ");
                            }
                            broadcastMessage(inboundMsg);
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
            } else if (e.getMessage().contains("Socket closed")) {
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

    // send msg to all players except the sender
    public void broadcastMessage(Message msg) {
        for (ClientHandler handler : server.handlers) {
            try {
                if (!handler.clientUsername.equals(clientUsername)) {
                    handler.output.writeObject(msg);
                    handler.output.flush(); // send any buffered output bytes
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
