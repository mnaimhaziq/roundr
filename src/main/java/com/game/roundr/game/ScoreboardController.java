package com.game.roundr.game;

import com.game.roundr.App;
import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class ScoreboardController {
    @FXML
    private Label timerLabel;
    private Timeline timeline;
    private int remainingTime = 10;

    public void initialize() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), this::updateTimer));
        timeline.setCycleCount(remainingTime); // Run the timeline for the specified number of seconds
        timeline.setOnFinished(this::handleTimerFinished);
        startTimer();
    }

    private void updateTimer(ActionEvent event) {
        remainingTime--;
        timerLabel.setText("You will be redirected to the lobby in " + Integer.toString(remainingTime) + "(s)");
    }

    private void handleTimerFinished(ActionEvent event) {
        redirectToMainMenu();
    }

    @FXML
    private void redirectToMainMenu() {
        try {
            App.setScene("MainMenu");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void startTimer() {
        timeline.play();
    }
}