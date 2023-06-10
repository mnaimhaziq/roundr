package com.game.roundr.game;

import com.game.roundr.App;
import com.game.roundr.DatabaseConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import java.io.BufferedReader;
import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainGameAreaController {

    @FXML
    private Label roundLabel;

    @FXML
    private Label randomWord;

    @FXML
    private TextField submitText;

    @FXML
    private Label timeLimitLabel;

    @FXML
    private Button endGameButton;

    @FXML
    private Button leaveGameButton;

    @FXML
    private Button submitButton;
    @FXML
    private ListView playerNameList;
    @FXML
    private ListView scoreList;

    private int roundCount = 1;
    private int roundLimit;
    private int timeLimit;
    private int wordLength;
    private int playerCount; // Turn order of players
    private int playerLimit; // Total number of players
    private Map<String, Integer> playerScore;
    private long startTime;
    private int gameId;
    private Timeline timer;

    public void initialize() {

        try {
            Connection conn = new DatabaseConnection().getConnection();

            // Get game information from database
            PreparedStatement stmt = conn.prepareStatement("SELECT turn_rounds, turn_time_limit, " +
                    "word_length, player_limit, player_count " +
                    "FROM game " +
                    "JOIN player_game ON game.game_id = player_game.game_id");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                // Set initial values for round count, time limit, and word length

                roundLimit = rs.getInt("turn_rounds");
                timeLimit = rs.getInt("turn_time_limit");
                wordLength = rs.getInt("word_length");
                playerLimit = rs.getInt("player_limit");
                playerCount = rs.getInt("player_count");

            }

            // Close the result set, statement, and connection
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println(App.username);

        // Initialize Player - Score Map
        playerScore = new HashMap<>();

        // Initialize score
        for (int i=1; i <= playerLimit; i++) {
            playerScore.put(Integer.toString(i), 0);
        }

        //render in-match scoreboard
        renderLiveScoreboard();

        // Update UI labels with initial values
        updateLabels();

        // Handle submit button click action
        submitButton.setOnAction(event -> {
            clickSubmitButton();
        });

        // Start the first player's turn
        startPlayerTurn();
    }

    int getPlayers(){
        return playerLimit;
    }

    private double calculateScore(){
        // score = configured time limit - time taken by the player to write the correct word
        long currentTime = System.currentTimeMillis();
        long timeTaken = (currentTime - startTime) / 1000; // Convert to seconds
        return timeLimit - timeTaken;
    }

    // Handle submit button
    private void clickSubmitButton(){
        if(isMatched()){
            System.out.println("Point Up");
            try {
                handlePlayerTurnEnd();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Try Again");
        }
    }

    // Check if matched?
    private boolean isMatched(){
        if(submitText.getText().equals(randomWord.getText())){
//            System.out.println("correct");
            return true;
        } else {
//            System.out.println("incorrect");
            return false;
        }
    }

    //render in-match scoreboard
    void renderLiveScoreboard(){
        // clear score in scoreboard
        playerNameList.getItems().clear();
        scoreList.getItems().clear();
        // add new score in scoreboard
        playerNameList.getItems().addAll(new ArrayList(playerScore.keySet()));
        scoreList.getItems().addAll(new ArrayList(playerScore.values()));
    };

    private void updateLabels() {
        roundLabel.setText("ROUND " + roundCount);
        timeLimitLabel.setText("Time Limit: " + timeLimit + " seconds");
    }

    private String getRandomWord(){
        try {
            // Create URL object with the API endpoint
            URL url = new URL("https://random-word-api.vercel.app/api?words=1&length=" + wordLength + "");

            // Create HttpURLConnection object and open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set request method to GET
            connection.setRequestMethod("GET");

            // Get the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Create BufferedReader to read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                // Read the response line by line
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the JSON response using Gson
                Gson gson = new Gson();
                String[] words = gson.fromJson(response.toString(), String[].class);

                // Extract the word from the array
                String word = words[0];

                return word;
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void startPlayerTurn() {
        // Logic to start the current player's turn
        System.out.println("Player " + playerCount + "'s turn");

        // random word generator
        randomWord.setText(getRandomWord());

        // Start the timer for the player's turn
        startTimer();
    }

    void startTimer() {
        AtomicInteger remainingTime = new AtomicInteger(timeLimit);

        // Update the UI with the remaining time
        timeLimitLabel.setText("Time Limit: " + remainingTime + " seconds");

        // Create a timeline for the timer
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingTime.getAndDecrement();
            timeLimitLabel.setText("Time Limit: " + remainingTime + " seconds");

            if (remainingTime.get() <= 0) {
                // Time is up, end the player's turn
//                stopTimer();
                try {
                    handlePlayerTurnEnd();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }));

        timer.setCycleCount(timeLimit);
        timer.play();
        startTime = System.currentTimeMillis();
    }

    void stopTimer() {
        if (timer != null) {
            timer.stop();
            System.out.println("Score: " + calculateScore());
            //increment score
            playerScore.put(Integer.toString(playerCount), playerScore.get(Integer.toString(playerCount))+(int)calculateScore());
            //reload scoreboard
            renderLiveScoreboard();
        }
    }

    private void handlePlayerTurnEnd() throws IOException {
        // calculate score and update game state

        // Stop the timer
        stopTimer();

        // Increment the currentPlayer for the next turn
        playerCount++;

        if (roundCount == roundLimit && playerCount > playerLimit) {
            // All players have finished their turns and all rounds have finished
            endGame();

        } else if (playerCount > playerLimit){
            // All players have finished their turns
            playerCount = 1;
            startNextRound();
        } else {
            // Start the next player's turn
            startPlayerTurn();
        }
    }

    private void startNextRound() {
        // Logic to start the next round
        roundCount++;
        System.out.println("Round " + roundCount + " started!");

        // ... codes

        updateLabels(); // Update the labels to reflect the new round

        // Start the first player's turn for the new round
        startPlayerTurn();
    }

    private void endGame() throws IOException {
        // calculate final score
        System.out.println("Game ended!");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Scoreboard.fxml"));
            Parent toScoreboard = loader.load();
            ScoreboardController toScoreboardController = loader.getController();

            // Pass the playerScore data to the ScoreboardController
            toScoreboardController.initData(playerScore);

            // Assuming you have a reference to the current Scene
            Scene currentScene = playerNameList.getScene();

            // Replace the root node of the current Scene with the Scoreboard view
            currentScene.setRoot(toScoreboard);

        } catch (IOException e) {
            e.printStackTrace();
        }

//        App.setScene("game/Scoreboard");
    }

    @FXML
    private void handleEndGameButton() {
        // Pause the timer
        timer.pause();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EndGamePopup.fxml"));
            Parent popupRoot = loader.load();
            EndGamePopupController popupController = loader.getController();

            // Pass the timer and resume function to the popup controller
            popupController.initData(timer, new Runnable() {
                @Override
                public void run() {
                    resumeTimer();
                }
            });

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(endGameButton.getScene().getWindow());
            popupStage.setScene(new Scene(popupRoot));
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void resumeTimer() {
        timer.play();
    }

    @FXML
    private void handleLeaveGameButton() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LeaveGamePopup.fxml"));
            Parent popupRoot = loader.load();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(leaveGameButton.getScene().getWindow());
            popupStage.setScene(new Scene(popupRoot));
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}