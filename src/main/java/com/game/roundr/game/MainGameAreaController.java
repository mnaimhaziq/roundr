package com.game.roundr.game;

import com.game.roundr.App;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    private int roundCount;
    private int roundLimit;
    private int timeLimit;
    private int currentPlayer = 1;
    private int totalPlayers = 4; // Change this to the actual number of players

    private Timeline timer;

    public void initialize() {
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

        // Add your code here to generate a random word and display it on the screen for the current player's turn

        // Start the timer for the player's turn
        startTimer();
    }

    private void startTimer() {
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

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void handlePlayerTurnEnd() throws IOException {
        // Add your logic to handle the end of the player's turn, such as calculating the score and updating game state

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

        // Add your code here to reset any necessary game variables or UI elements for the new round

        updateLabels(); // Update the labels to reflect the new round

        // Start the first player's turn for the new round
        startPlayerTurn();
    }

    private void endGame() throws IOException {
        handleEndGameButton();
    }


    @FXML
    private void handleEndGameButton() throws IOException {
        // Add your logic to calculate the final score, determine the winner, and perform any necessary actions
        // For now, let's just print a message
        System.out.println("Game ended!");
        App.setScene("game/Scoreboard");
    }

    @FXML
    private void handleLeaveGameButton() throws IOException {
        // Add your logic to handle leaving the game, such as cleaning up game data or notifying other players
        // For now, let's just print a message
        System.out.println("Left the game!");
        App.setScene("lobby/JoinLobby");
    }
}