package com.game.roundr.lobby;

import com.game.roundr.App;
import com.game.roundr.DatabaseConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
        decrementTemp();
    }

    @FXML
    private void handleReadyButton() throws IOException{
        App.setScene("game/MainGameArea");
    }
    
    private void decrementTemp() {
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement("UPDATE game SET player_count = player_count - 1 WHERE game_id = (SELECT MAX(game_id) FROM game)");
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
