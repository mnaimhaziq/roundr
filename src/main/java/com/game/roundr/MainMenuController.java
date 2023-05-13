package com.game.roundr;

import java.io.IOException;
import javafx.fxml.FXML;

public class MainMenuController {

    @FXML
    public void tempSwitch() throws IOException {
        App.setScene("lobby/join_lobby");
    }

}
