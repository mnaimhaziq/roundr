package com.game.roundr.game;

import com.game.roundr.App;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class MainGameAreaController {

    @FXML
    private Label roundLabel;
    @FXML
    private Label timeLimitLabel;

    @FXML
    private Button endGameButton;

    @FXML
    private Button leaveGameButton;

    @FXML
    private Button submitButton;

    @FXML
    private AnchorPane rootPane; // Reference to the root pane of the popup

    private int roundCount;
    private int roundLimit;
    private int timeLimit;
    private int currentPlayer = 1;
    final int totalPlayers = 4; // Change this to the actual number of players

    private Timeline timer;

    public void initialize() {
        System.out.println("MainGameAreaController initialized");
        // Set initial values for round count, time limit, and word length
        roundCount = 1;
        timeLimit = 10;
        roundLimit = 5;

        // Update UI labels with initial values
        updateLabels();

        submitButton.setOnAction(event -> {
            try {
                handlePlayerTurnEnd();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        // Start the first player's turn
        startPlayerTurn();

    }


    private void updateLabels() {
        roundLabel.setText("ROUND " + roundCount);
        timeLimitLabel.setText("Time Limit: " + timeLimit + " seconds");
    }

    private void startPlayerTurn() {
        // Logic to start the current player's turn
        System.out.println("Player " + currentPlayer + "'s turn");

        // random word generator

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
                stopTimer();
                try {
                    handlePlayerTurnEnd();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }));

        timer.setCycleCount(timeLimit);
        timer.play();
    }

    void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    void resumeTimer() {
        timer.play();
    }

    private void handlePlayerTurnEnd() throws IOException {
        // calculate score and update game state

        // Stop the timer
        stopTimer();

        // Increment the currentPlayer for the next turn
        currentPlayer++;

        if (roundCount == roundLimit && currentPlayer > totalPlayers) {
            // All players have finished their turns and all rounds have finished
            endGame();

        } else if (currentPlayer > totalPlayers){
            // All players have finished their turns
            currentPlayer = 1;
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
        App.setScene("game/Scoreboard");
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