package com.game.roundr.network;

import com.game.roundr.App;
import com.game.roundr.DatabaseConnection;
import com.game.roundr.game.EndGamePopupController;
import com.game.roundr.game.MainGameAreaController;
import com.game.roundr.lobby.GameLobbyController;
import com.game.roundr.models.Player;
import com.game.roundr.models.Message;
import com.game.roundr.models.MessageType;
import com.google.gson.Gson;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import javafx.application.Platform;

public class ClientHandler implements Runnable {

    private Socket socket;
    private Server server;
    protected ObjectInputStream input;
    protected ObjectOutputStream output;
    private String clientUsername;

    private MainGameAreaController mgac;
    private boolean isTimerRunning = true;
    private Timeline timer;

    public static ArrayList<ObjectOutputStream> writers;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            this.writers = new ArrayList<ObjectOutputStream>();
            this.writers.add(null);
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

                                // TODO: add the message to the chat
                                System.out.println("Chat: " + inboundMsg.getSenderName() + " has joined");

                                //add writer to list
                                writers.add(this.output);
                                System.out.println(writers);
                            }
                            break;
                        }
                        case DISCONNECT -> {
                            // TODO: add the message to the chat area
                            System.out.println("Chat: " + inboundMsg.getSenderName() + " has left");

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

                            GameLobbyController gameLobbyController = App.glc;
                            MainGameAreaController mainGameAreaController = App.mainGameAreaController;
                             if (gameLobbyController != null ) {
                                gameLobbyController.addToTextArea(inboundMsg);

                            }
                            if( mainGameAreaController != null) {
                                mainGameAreaController.addToTextArea(inboundMsg);
                            }
                            // forward the chat message
                            broadcastMessage(inboundMsg);

                            break;
                        }
                        case END_GAME -> {
                            MainGameAreaController mainGameAreaController = App.mainGameAreaController;
                            if (mainGameAreaController != null) {
                                mainGameAreaController.passedEndGamePopup(inboundMsg);
                                System.out.println("Client Handler: not null");
                            } else {
                                System.out.println("Client Handler: null");
                            }
                            broadcastMessage(inboundMsg);
                            break;
                        }
                        case RANDOM_WORD -> {
                            MainGameAreaController mainGameAreaController = App.mainGameAreaController;
                            if (mainGameAreaController != null) {
                                mainGameAreaController.generateWordPass(inboundMsg);
                                System.out.println("Client Handler: not null " + inboundMsg.getContent());
                            } else {
                                System.out.println("Client Handler: null " + inboundMsg.getContent());
                            }
                            // forward the chat message
                            broadcastMessage(inboundMsg);
                            break;
                        }
                        case PLAYER_SCORE -> {
                            // add the message to the chat textArea
                            MainGameAreaController mainGameAreaController = App.mainGameAreaController;
                            if (mainGameAreaController != null) {
                                mainGameAreaController.passedScorePass(inboundMsg);
                                System.out.println("Client Handler PlayerScore: not null ");
                            } else {
                                System.out.println("Client Handler PlayerScore: null ");
                            }
                            // forward the chat message
                            broadcastMessage(inboundMsg);
                            break;
                        }
                        case TURN -> {
                            // add the message to the chat textArea
                            MainGameAreaController mainGameAreaController = App.mainGameAreaController;
                            if (mainGameAreaController != null) {
                                mainGameAreaController.passedShiftedTurn(inboundMsg);
                                System.out.println("Client Handler Turn: not null ");
                            } else {
                                System.out.println("Client Handler Turn: null ");
                            }
                            // forward the chat message
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

//    private Message getRandomWord(int wordLength){
//
//
//        try {
//
//            // Create URL object with the API endpoint
//            URL url = new URL("https://random-word-api.vercel.app/api?words=1&length=" + wordLength + "");
//
//            // Create HttpURLConnection object and open connection
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//            // Set request method to GET
//            connection.setRequestMethod("GET");
//
//            // Get the response code
//            int responseCode = connection.getResponseCode();
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                // Create BufferedReader to read the response
//                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//
//                // Read the response line by line
//                String line;
//                StringBuilder response = new StringBuilder();
//                while ((line = reader.readLine()) != null) {
//                    response.append(line);
//                }
//                reader.close();
//
//                // Parse the JSON response using Gson
//                Gson gson = new Gson();
//                String[] words = gson.fromJson(response.toString(), String[].class);
//
//                // Extract the word from the array
//                String word = words[0];
//
//                Message message = new Message(MessageType.RANDOM_WORD,App.username, word);
//                return message;
//            } else {
//                System.out.println("GET request failed. Response Code: " + responseCode);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

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
            // Pause or resume the timer based on the updated state
            if (!isTimerRunning) {
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

    public void resumeTimer() {
        // Initialize the timer
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            // Timer logic here
        }));

        // Call the play() method
        timer.play();
        isTimerRunning = true;
    }

    public String getUsername() {
        return clientUsername;
    }

}
