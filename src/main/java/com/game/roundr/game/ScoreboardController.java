package com.game.roundr.game;

import com.game.roundr.App;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class ScoreboardController {
    @FXML
    private Label timerLabel;

    @FXML
    private Label firstPlayer;

    @FXML
    private Label firstScore;

    @FXML
    private Label secondPlayer;

    @FXML
    private Label secondScore;

    @FXML
    private Label thirdPlayer;

    @FXML
    private Label thirdScore;

    private Timeline timeline;
    private int remainingTime = 10;
    private boolean isTimerFinished = false;
    private Map<String, Integer> playerScore;

    public void initData(Map<String, Integer> playerScore){
        this.playerScore = playerScore;
        loadData();
    }

    public void loadData(){
        if(playerScore != null) {
            // Sort the playerScore map by value in decreasing order
            List<Map.Entry<String, Integer>> sortedScores = playerScore.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toList());

            // Retrieve the top three entries from the sorted list
            Map.Entry<String, Integer> firstEntry = sortedScores.size() >= 1 ? sortedScores.get(0) : null;
            Map.Entry<String, Integer> secondEntry = sortedScores.size() >= 2 ? sortedScores.get(1) : null;
            Map.Entry<String, Integer> thirdEntry = sortedScores.size() >= 3 ? sortedScores.get(2) : null;

            // Assign the values to the respective variables
            firstPlayer.setText(firstEntry != null ? firstEntry.getKey() : null);
            firstScore.setText(String.valueOf(firstEntry != null ? firstEntry.getValue() : null));

            secondPlayer.setText(secondEntry != null ? secondEntry.getKey() : null);
            secondScore.setText(String.valueOf(secondEntry != null ? secondEntry.getValue() : null));

            thirdPlayer.setText(thirdEntry != null ? thirdEntry.getKey() : "-");
            thirdScore.setText(String.valueOf(thirdEntry != null ? thirdEntry.getValue() : null));
        } else {
            System.out.println("PlayerScoreArray is null");
        }
    }

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
        if (!isTimerFinished) {
            redirectToMainMenu();
        }
    }

    @FXML
    private void handleToMainMenuButton() {
        isTimerFinished = true;
        redirectToMainMenu();
    }

    @FXML
    private void redirectToMainMenu() {
        try {
            // Handle server disconnection
            if (App.server != null) {
                App.server.closeServer();
                App.server = null;
            }
            // Handle client disconnection
            else if (App.client != null) {
                App.client.closeClient();
                App.client = null;
            }
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