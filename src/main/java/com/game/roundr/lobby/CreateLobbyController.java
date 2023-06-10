package com.game.roundr.lobby;

import com.game.roundr.App;
import com.game.roundr.DatabaseConnection;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import com.game.roundr.network.Server;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

public class CreateLobbyController implements Initializable {

    @FXML
    private ComboBox<Integer> roundsBox;
    @FXML
    private ComboBox<Integer> timeBox;
    @FXML
    private ComboBox<Integer> playerBox;
    @FXML
    private ComboBox<Integer> wordBox;

    @FXML
    private void handleMainMenuButtonClick() throws IOException {
        App.setScene("MainMenu");
    }

    @FXML
    private void handleCreateLobbyButtonClick() throws IOException {
        App.server = new Server(App.username);
        App.server.startServer();

        // update database tables
        try {
            Connection conn = new DatabaseConnection().getConnection();

            // create game entry in the database
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO game"
                    + "(turn_rounds, "
                    + "turn_time_limit, "
                    + "word_length, "
                    + "player_limit, "
                    + "player_count, "
                    + "ip_address) "
                    + "VALUES(?, ?, ?, ?, ?, ?)");
            stmt.setInt(1, roundsBox.getValue());
            stmt.setInt(2, timeBox.getValue());
            stmt.setInt(3, wordBox.getValue());
            stmt.setInt(4, playerBox.getValue());
            stmt.setInt(5, 1);
            stmt.setString(6,
                    InetAddress.getLocalHost().getHostAddress());
            stmt.executeUpdate();

            // create player_game entry for host
            stmt = conn.prepareStatement("INSERT INTO player_game(game_id,"
                    + "player_id, is_host, player_color, final_score) "
                    + "SELECT MAX(game.game_id), player.player_id, 1,?, 0 "
                    + "FROM game "
                    + "JOIN player ON game.ip_address = player.ip_address "
                    + "WHERE game.ip_address = ? "
                    + "AND player.username = ?");
            stmt.setString(1, App.getHexColorCode());
            stmt.setString(2,
                    InetAddress.getLocalHost().getHostAddress());
            stmt.setString(3, App.username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) { // When setScene
        // Add options to combo boxes
        ObservableList<Integer> ol = FXCollections.observableArrayList();
        for (int i = 0; i < 10; i++) {
            ol.add(i + 1);
        }
        roundsBox.getItems().addAll(ol);
        ol.clear();
        for (int i = 1; i < 9; i++) {
            ol.add(i + 1);
        }
        wordBox.getItems().addAll(ol);
        ol.clear();
        for (int i = 0; i < 60; i++) {
            ol.add(i + 1);
        }
        timeBox.getItems().addAll(ol);
        playerBox.getItems().addAll(2, 3, 4, 5, 6);

        // Set default values
        roundsBox.setValue(2);
        wordBox.setValue(3);
        timeBox.setValue(5);
        playerBox.setValue(2);
    }

}
