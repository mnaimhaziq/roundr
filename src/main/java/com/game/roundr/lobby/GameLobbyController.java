package com.game.roundr.lobby;

import com.game.roundr.App;
import java.io.IOException;
import javafx.fxml.FXML;

public class GameLobbyController {

    @FXML
    private void handleLeaveLobbyButtonClick() throws IOException {
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
    }

    @FXML
    private void handleReadyButton() throws IOException{
        App.setScene("game/MainGameArea");
    }

}

