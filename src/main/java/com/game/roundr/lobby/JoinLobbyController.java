package com.game.roundr.lobby;

import com.game.roundr.App;
import java.io.IOException;
import javafx.fxml.FXML;

public class JoinLobbyController {

    @FXML
    private void handleMainMenuButtonClick() throws IOException {
        App.setScene("MainMenu");
    }

}
