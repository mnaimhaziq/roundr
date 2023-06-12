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
import java.util.Arrays;
import javafx.scene.control.Alert;

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
            // setup client socket
            socket = new Socket(address, port);
            client.output = new ObjectOutputStream(socket.getOutputStream());
            client.input = new ObjectInputStream(socket.getInputStream());
            System.out.println("Client: Running. Username: " + client.username);

            // send CONNECT msg to game server
            Message msg = new Message(MessageType.CONNECT, client.username, "");
            client.sendMessage(msg);

            // listen for messages
            while (this.socket.isConnected()) {
                Message inboundMsg = (Message) client.input.readObject();

                if (inboundMsg != null) {
                    System.out.println("Client: Received " + inboundMsg.toString());

                    // handle msgs based on their type
                    switch (inboundMsg.getMsgType()) {
                        case CONNECT_FAILED -> {
                            App.showAlert(Alert.AlertType.INFORMATION,
                                    "Connection Failed", inboundMsg.getContent());
                            break;
                        }
                        case CONNECT_OK -> {
                            // go to game lobby
                            App.setScene("lobby/GameLobby");

                            // reset the list of players
                            App.glc.clearPlayers();

                            // set the gamecode and host
                            String[] msgContent = inboundMsg.getContent().split(";");
                            App.glc.SetLobbyInfo(
                                    msgContent[msgContent.length - 1], msgContent[msgContent.length - 2]);

                            // extract gamecode and host
                            String[] pDetails = Arrays.copyOfRange(msgContent, 0, msgContent.length - 2);
                            inboundMsg.setContent(String.join(";", pDetails)); // clean player

                            // fetch the list of players
                            App.glc.updatePlayers(inboundMsg);

                            // add the message to the chat
                            App.glc.addToTextArea("Server: " + client.username + " has joined");
                            break;
                        }
                        case USER_JOINED -> {
                            // reset the list of players
                            App.glc.clearPlayers();

                            // fetch the list of players
                            App.glc.updatePlayers(inboundMsg);

                            // add the message to the chat
                            App.glc.addToTextArea("Server: " + inboundMsg.getSenderName() + " has joined");
                            break;
                        }
                        case DISCONNECT -> {
                            if (inboundMsg.getContent().equals("Server closed")) {
                                App.showAlert(Alert.AlertType.INFORMATION,
                                        "Connection Closed", inboundMsg.getContent());
                                App.client = null;

                                // switch scene
                                App.setScene("MainMenu");
                            } else { // a player disconnected
                                // reset the list of players
                                App.glc.clearPlayers();

                                // fetch the list of players
                                App.glc.updatePlayers(inboundMsg);

                                // add the message to the chat
                                App.glc.addToTextArea("Server: " + inboundMsg.getSenderName() + " has left");
                            }
                            break;
                        }
                        case READY -> {
                            // update player inside server list
                            App.glc.updatePlayer(inboundMsg);

                            // if all ready, then start
                            if (App.glc.isAllReady()) {
                                App.setScene("game/MainGameArea");
                            }
                            break;
                        }
                        case CHAT -> {
                            // add the message to the chat textArea
                            if (App.glc != null) {
                                App.glc.addToTextArea(inboundMsg);

                            } else if (App.mgac != null) {
                                App.mgac.addToTextArea(inboundMsg);
                            }
                            break;
                        }
                        case END_GAME -> {
                            App.mgac.passedEndGamePopup(inboundMsg);
                            break;
                        }
                        case RANDOM_WORD -> {
                            if (App.mgac != null) {
                                App.mgac.generateWordPass(inboundMsg);
                                System.out.println("Client Listener: not null " + inboundMsg.getContent());
                            } else {
                                System.out.println("Client Listener: null " + inboundMsg.getContent());
                            }
                            break;
                        }
                        case PLAYER_SCORE -> {
                            if (App.mgac != null) {
                                App.mgac.passedScorePass(inboundMsg);
                                System.out.println("Client Listener PlayerScore: not null ");
                            } else {
                                System.out.println("Client Listener PlayerScore: null ");
                            }
                            break;
                        }
                        case TURN -> {
                            if (App.mgac != null) {
                                App.mgac.passedShiftedTurn(inboundMsg);
                                System.out.println("Client Listener Turn: not null ");
                            } else {
                                System.out.println("Client Listener Turn: null ");
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
            if (e instanceof ConnectException) {
                System.out.println("Client: Connection failed");
            } else if (e.getMessage().equals("Connection reset")) {
                System.out.println("Client: Connection closed");
            }
        } catch (IOException e) {
            System.out.println("Client: Connection closed");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            App.client = null; // remove client socket if there is error
        }
    }
}
