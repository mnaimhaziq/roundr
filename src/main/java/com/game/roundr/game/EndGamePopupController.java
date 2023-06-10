package com.game.roundr.game;

import com.game.roundr.App;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import com.game.roundr.game.MainGameAreaController;

import java.io.IOException;

public class EndGamePopupController {
    @FXML
    private Button yesButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label playersLabel;

    private Timeline timer;
    private Runnable resumeFunction;

    public void initData(Timeline timer, Runnable resumeFunction) {
        this.timer = timer;
        this.resumeFunction = resumeFunction;
    }

//    public void initialize(){
//        updateLabel();
//    }
//
//    private void updateLabel(){
//        // Get number of votes
//        MainGameAreaController mgac = new MainGameAreaController(timer);
//        int totalPlayers = mgac.getPlayers();
//        playersLabel.setText(totalPlayers + " players voted");
//    }

    @FXML
    private void handleYesButton() throws IOException {

        // Stop the timer
        timer.stop();

        // Close the popup
        Stage popupStage = (Stage) yesButton.getScene().getWindow();
        popupStage.close();
        System.out.println("The game has ended!");
        App.setScene("game/Scoreboard");
    }

    @FXML
    private void handleCancelButton() {

        // Close the popup
        Stage popupStage = (Stage) cancelButton.getScene().getWindow();
        popupStage.close();

        // Resume the timer
        if (resumeFunction != null) {
            resumeFunction.run();
        }
    }

}
