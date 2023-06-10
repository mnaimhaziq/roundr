package com.game.roundr.lobby;

import com.game.roundr.App;
import com.game.roundr.DatabaseConnection;
import com.game.roundr.models.Game;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import com.game.roundr.network.Client;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class JoinLobbyController implements Initializable {

    @FXML
    private TextField codeTextField;

    @FXML
    private ListView<Game> lobbyList;

    @FXML
    private void handleRefreshButtonClick() throws IOException {
        lobbyList.getItems().clear();
        getLobbies();
    }

    @FXML
    private void handleMainMenuButtonClick() throws IOException {
        App.setScene("MainMenu");
    }

    @FXML
    private void handleJoinLobbyButtonClick() {
        // get the game code
        String gameCode = codeTextField.getText();

        // if game code exists, get its IP address
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT ip_address FROM game WHERE game_id = ?");
            stmt.setString(1, gameCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                App.client = new Client(rs.getString("ip_address"), App.username); // con
                App.client.startClient();

                // Create player_game entry in the db
                stmt = conn.prepareStatement("INSERT INTO player_game (game_id,"
                        + "player_id, is_host, player_color, final_score) "
                        + "SELECT game.game_id, player.player_id, '1', ?, '0' "
                        + "FROM game "
                        + "JOIN player ON game.ip_address = player.ip_address "
                        + "WHERE game.ip_address = ? "
                        + "AND player.username = ?");
                stmt.setString(1, getHexColorCode());
                stmt.setString(2, InetAddress.getLocalHost().getHostAddress()); // Sets private IP
                stmt.setString(3, App.username);
                stmt.executeUpdate();
            }
        } catch (SQLException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private String getHexColorCode() {
        // Colour in the pastel range (120-230)
        Random random = new Random();
        int r = random.nextInt(111) + 120;
        int g = random.nextInt(111) + 120;
        int b = random.nextInt(111) + 120;

        return String.format("#%02X%02X%02X", r, g, b);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        getLobbies();
        // Cell style
        lobbyList.setCellFactory((ListView<Game> l) -> new LobbyCell());
    }

    private class LobbyCell extends ListCell<Game> {

        @Override
        protected void updateItem(Game item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                setGraphic(createGraphic(item));
            }
        }

    }

    // Generate lobby list cell nodes
    private Node createGraphic(Game game) {
        HBox hBox = new HBox(40.0);
        hBox.setPrefHeight(60.0);
        hBox.setAlignment(Pos.CENTER);

        Circle circle = new Circle(15.0);
        if (game.getNumOfPlayers() < game.getPlayerLimit()) {
            circle.setFill(Color.web("#A1FF89"));
        } else {
            circle.setFill(Color.web("#FF7171"));
        }

        Text lobbyName = new Text(game.getHostName() + "'s Lobby");
        lobbyName.setFont(new Font("Inter Bold", 18.0));
        lobbyName.setWrappingWidth(320.0);

        Text playerCount = new Text(game.getNumOfPlayers()
                + "/"
                + game.getPlayerLimit());
        playerCount.setFont(new Font("Inter Bold", 18.0));

        hBox.getChildren().addAll(circle, lobbyName, playerCount);

        hBox.setOnMouseClicked((MouseEvent event) -> {
            App.client = new Client(game.getIpAddress(), App.username);
            App.client.startClient();

            try {
                // Create player_game entry in the db
                Connection conn = new DatabaseConnection().getConnection();
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO player_game (game_id,"
                        + "player_id, is_host, player_color, final_score) "
                        + "SELECT game.game_id, player.player_id, '1', ?, '0' "
                        + "FROM game "
                        + "JOIN player ON game.ip_address = player.ip_address "
                        + "WHERE game.ip_address = ? "
                        + "AND player.username = ?");
                stmt.setString(1, getHexColorCode());
                stmt.setString(2, InetAddress.getLocalHost().getHostAddress()); // Sets to private IP
                stmt.setString(3, App.username);
                stmt.executeUpdate();
            } catch (SQLException | UnknownHostException e) {
                e.printStackTrace();
            }
        });

        return hBox;
    }

    private void getLobbies() {
        try {
            ObservableList<Game> gameData = FXCollections.observableArrayList();

            ResultSet rs = new DatabaseConnection()
                    .getConnection()
                    .prepareStatement("""
                                      SELECT game.*, player.username AS host_name FROM `game` 
                                      JOIN player_game ON game.game_id = player_game.game_id
                                      JOIN player ON player.player_id = player_game.player_id 
                                      AND player_game.is_host = '1' LIMIT 10;""")
                    .executeQuery();
            while (rs.next()) {
                gameData.add(new Game(
                        rs.getInt("game_id"),
                        rs.getString("game_status"),
                        rs.getInt("turn_rounds"),
                        rs.getInt("turn_time_limit"),
                        rs.getInt("word_length"),
                        rs.getInt("player_limit"),
                        rs.getInt("player_count"),
                        rs.getString("ip_address"),
                        rs.getString("host_name")
                ));
            }
            // Set the lobbies into the list
            lobbyList.setItems(gameData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
