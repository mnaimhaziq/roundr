package com.game.roundr.lobby;

import com.game.roundr.App;
import java.io.IOException;

import com.game.roundr.networking.Client;
import com.game.roundr.networking.Server;
import javafx.fxml.FXML;

public class GameLobbyController {



    @FXML
    private void handleLeaveLobbyButtonClick() throws IOException {
        // Disconnect the server or client based on the role
        System.out.println("client" + App.client);
        if (App.playerRole.equals("Server")) {
            App.server.CloseServer();
            App.server = null;
            App.setScene("MainMenu");
        }

        else if (App.playerRole.equals("Client")) {
            App.client.CloseClient();
            App.client = null;
            App.setScene("MainMenu");
        }
    }




    @FXML
    private void handleReadyButton() throws IOException{
        App.setScene("game/MainGameArea");
    }



}

