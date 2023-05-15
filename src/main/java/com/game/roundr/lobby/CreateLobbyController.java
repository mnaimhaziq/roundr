package com.game.roundr.lobby;

import com.game.roundr.App;
import java.io.IOException;
import javafx.fxml.FXML;

public class CreateLobbyController {

    @FXML
    public void handleMainMenuButtonClick() throws IOException {
        App.setScene("MainMenu");
    }

    public void handleCreateLobbyButtonClick() throws IOException {
        App.setScene("lobby/GameLobby");
    }

}
