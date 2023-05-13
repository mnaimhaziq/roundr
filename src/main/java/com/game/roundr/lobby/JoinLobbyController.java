package com.game.roundr.lobby;

import com.game.roundr.App;
import java.io.IOException;
import javafx.fxml.FXML;

public class JoinLobbyController {

    @FXML
    private void tempSwitch() throws IOException {
        App.setScene("main_menu");
    }

}
