package com.game.roundr.game;

import com.game.roundr.App;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class LeaveGamePopupController {
    @FXML
    private Button yesButton;

    @FXML
    private Button cancelButton;

    @FXML
    private void handleYesButton() throws IOException {
        // Close the popup
        Stage popupStage = (Stage) yesButton.getScene().getWindow();
        popupStage.close();
        System.out.println("Successfully Leave the game!");
        App.setScene("lobby/JoinLobby");
    }

    @FXML
    private void handleCancelButton() {
        // Close the popup
        Stage popupStage = (Stage) cancelButton.getScene().getWindow();
        popupStage.close();
    }
}
