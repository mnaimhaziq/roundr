package com.game.roundr.game;

import com.game.roundr.App;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class MainGameAreaController {

    @FXML
    private void handleEndGameButton() throws IOException {
        App.setScene("game/Scoreboard");
    }

    public void handleLeaveGameButton() throws IOException{
        App.setScene("lobby/JoinLobby");
    }
}
