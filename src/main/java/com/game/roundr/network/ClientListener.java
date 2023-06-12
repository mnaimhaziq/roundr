package com.game.roundr.network;

import com.game.roundr.App;
import com.game.roundr.game.EndGamePopupController;
import com.game.roundr.game.MainGameAreaController;
import com.game.roundr.lobby.GameLobbyController;
import com.game.roundr.models.Message;
import com.game.roundr.models.MessageType;
import com.game.roundr.models.Player;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Map;
import javafx.scene.control.Alert;

public class ClientListener implements Runnable {

    private Socket socket;
    private final String address;
    private final Client client;
    private final int port;

    private MainGameAreaController mgac;
    private boolean isTimerRunning = true;
    private Timeline timer;

    public ClientListener(String address, int port, Client client, MainGameAreaController mgac) {
        this.address = address;
        this.client = client;
        this.port = port;
        this.mgac = mgac;
    }

    // Method to set MainGameAreaController instance
    public void setMainGameAreaController(MainGameAreaController mgac) {
        this.mgac = mgac;
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

                            // TODO: add the message to the chat
                            System.out.println("Chat: " + client.username + " has joined");
                            break;
                        }
                        case USER_JOINED -> {
                            // reset the list of players
                            App.glc.clearPlayers();

                            // fetch the list of players
                            App.glc.updatePlayers(inboundMsg);

                            // TODO: add the message to the chat
                            System.out.println("Chat: " + inboundMsg.getSenderName() + " has joined");
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

                                // TODO: add msg to the chats
                                System.out.println("Chat: " + inboundMsg.getSenderName() + " has left");
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
                            GameLobbyController gameLobbyController = App.glc;
                            MainGameAreaController mainGameAreaController = App.mainGameAreaController;
                            if (gameLobbyController != null) {
                                gameLobbyController.addToTextArea(inboundMsg);
                                mainGameAreaController.addToTextArea(inboundMsg);
                            }
                            break;
                        }
                        case END_GAME -> {
                            // show modal
                            // pause timer

//                            // Handle end game message
//                            System.out.println("Requested to end the game");
//                            // Pause the timer and show the popup
//                            Platform.runLater(() -> {
//                                mgac.pauseTimer();
//                                showEndGamePopup();
//                            });
//                            break;


//                            // Update the shared variable based on the timer state
//                            boolean timerRunning = mgac.timer.getStatus() == Animation.Status.RUNNING;
//                            isTimerRunning = timerRunning;
//
//                            // Pause or resume the timer based on the updated state
//                            if (timerRunning) {
//                                mgac.getTimer().play();
//                            } else {
//                                mgac.getTimer().pause();
//                            }


//                            // Handle end game message
//                            System.out.println("Requested to end the game");
//
//                            // Show the end game popup
//                            Platform.runLater(this::showEndGamePopup);

                            MainGameAreaController mainGameAreaController = App.mainGameAreaController;
                            if (mainGameAreaController != null) {
                                mainGameAreaController.passedEndGamePopup(inboundMsg);
                                System.out.println("Client Listener: not null " + inboundMsg.getContent());
                            } else {
                                System.out.println("Client Listener: null " + inboundMsg.getContent());
                            }
                            break;
                        }
                        case RANDOM_WORD -> {
                            // add the message to the chat textArea
                            MainGameAreaController mainGameAreaController = App.mainGameAreaController;
                            if (mainGameAreaController != null) {
                                mainGameAreaController.generateWordPass(inboundMsg);
                                System.out.println("Client Listener: not null " + inboundMsg.getContent());
                            } else {
                                System.out.println("Client Listener: null " + inboundMsg.getContent());
                            }
                            break;
                        }
                        case PLAYER_SCORE -> {
                            // add the message to the chat textArea
                            MainGameAreaController mainGameAreaController = App.mainGameAreaController;
                            if (mainGameAreaController != null) {
                                mainGameAreaController.passedScorePass(inboundMsg);
                                System.out.println("Client Listener PlayerScore: not null ");
                            } else {
                                System.out.println("Client Listener PlayerScore: null ");
                            }
                            break;
                        }
                        case TURN -> {
                            // add the message to the chat textArea
                            MainGameAreaController mainGameAreaController = App.mainGameAreaController;
                            if (mainGameAreaController != null) {
                                mainGameAreaController.passedShiftedTurn(inboundMsg);
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

    private void showEndGamePopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/game/roundr/game/EndGamePopup.fxml"));
            Parent popupRoot = loader.load();
            EndGamePopupController popupController = loader.getController();

            // Pass the timer from MainGameAreaController to EndGamePopupController
            popupController.initData(mgac.getTimer(), this::resumeTimer);

            // Update the shared variable based on the timer state
            boolean timerRunning = mgac.timer.getStatus() == Animation.Status.RUNNING;
            isTimerRunning = timerRunning;

            System.out.println(timerRunning);
            System.out.println(mgac.getTimer());
            // Pause or resume the timer based on the updated state
            if (isTimerRunning) {
                mgac.getTimer().pause();
            }

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(popupRoot));

            // Create a new stage as the owner of the popup stage
            Stage ownerStage = new Stage();

            // Set the owner of the popup stage
            popupStage.initOwner(ownerStage);

            popupStage.showAndWait();

            // After the popup is closed, update the timer state based on the shared variable
            if (isTimerRunning) {
                mgac.getTimer().play();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTimer(Timeline timer) {
        this.timer = timer;
    }

    public void resumeTimer() {
        // Initialize the timer
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            // Timer logic here
        }));
        // Call the play() method
        timer.play();
        isTimerRunning = true;
    }

    private void sendWordMessage(Message message) {
        try {
            client.output.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendWordMessage(String content) {
        Message msg = new Message(MessageType.RANDOM_WORD, App.username, content);
        // send the message
        this.sendWordMessage(msg);
    }

    private void sendPlayerScore(Message message) {
        try {
            client.output.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Message message) {
        try {
            client.output.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPlayerScore(Map<String, Integer> playerScore) {
        Message msg = new Message(MessageType.PLAYER_SCORE, App.username, playerScore);
        // send the message
        this.sendPlayerScore(msg);
    }

    public void sendChatMessage(String content) {
        Message msg = new Message(MessageType.CHAT, App.username, content);

        // send the message
        this.sendMessage(msg);

    }

    public void sendShiftedTurn(String turn) {
        Message msg = new Message(MessageType.TURN, App.username, turn);
        // send the message
        this.sendShiftedTurn(msg);
    }

    private void sendShiftedTurn(Message message) {
        try {
            client.output.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendEndGameRequest() {
        Message msg = new Message(MessageType.END_GAME, App.username, "");
        // send the message
        this.sendEndGameRequest(msg);
    }

    private void sendEndGameRequest(Message message) {
        try {
            client.output.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
