package com.game.roundr.lobby;

import com.game.roundr.App;
import java.io.IOException;
import javafx.fxml.FXML;

public class GameLobbyController {

    @FXML
    public void handleLeaveLobbyButtonClick() throws IOException {
        App.setScene("MainMenu");
    }
    
}
