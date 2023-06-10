package com.game.roundr.lobby;

import com.game.roundr.App;
import com.game.roundr.DatabaseConnection;
import com.game.roundr.game.MainGameAreaController;
import com.game.roundr.models.Game;
import com.game.roundr.network.Client;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
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
    
    ObservableList<Game> games = FXCollections.observableArrayList();

    @FXML
    private void handleRefreshButtonClick() throws IOException {
        games.clear();
        getLobbies();
    }

    @FXML
    private void handleMainMenuButtonClick() throws IOException {
        App.setScene("MainMenu");
    }

    @FXML
    private void handleJoinLobbyButtonClick() throws IOException {
        // get the game code
        String gameCode = codeTextField.getText();

        // if the game exists, get game IP address and connect to lobby
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM game WHERE game_id = ?");
            stmt.setString(1, gameCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if (rs.getInt(6) < rs.getInt(5)) {
                    joinLobby(gameCode, rs.getString(8));
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING,
                            "The lobby is full.");
                    alert.showAndWait();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
  
    public void handleJoinLobbyButton() {
        MainGameAreaController mgac = new MainGameAreaController();
        App.client = new Client("localhost", App.username, mgac);
        App.client.startClient();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        getLobbies();
        lobbyList.setCellFactory((ListView<Game> l) -> new LobbyCell());
    }

    private class LobbyCell extends ListCell<Game> {

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

        hBox.setOnMouseClicked((MouseEvent e) -> {
            String gameCode = Integer.toString(game.getGameID());

            if (game.getNumOfPlayers() < game.getPlayerLimit()) {
                joinLobby(gameCode, game.getIpAddress());
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        "The lobby is full.");
                alert.showAndWait();
            }
        });

        return hBox;
    }

    private void getLobbies() {
        try {
            ResultSet rs = new DatabaseConnection()
                    .getConnection()
                    .prepareStatement("""
                        SELECT game.*, player.username AS `host_name` FROM game 
                        JOIN player_game ON game.game_id = player_game.game_id
                        JOIN player ON player.player_id = player_game.player_id 
                        AND player_game.is_host = '1' LIMIT 10;""")
                    .executeQuery();
            while (rs.next()) {
                games.add(new Game(
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
            lobbyList.setItems(games);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void joinLobby(String gameCode, String gameAddress) {
        MainGameAreaController mgac = new MainGameAreaController();
        App.client = new Client(gameAddress, App.username, mgac);
        App.client.startClient();

        // update database tables
        try {
            // create player_game entry in the db
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO player_game"
                    + "(game_id, player_id, is_host, player_color, final_score) "
                    + "SELECT game.game_id, player.player_id, 0, ?, 0 FROM game "
                    + "JOIN player WHERE game.game_id = ? AND player.username = ?");
            stmt.setString(1, App.getHexColorCode());
            stmt.setString(2, gameCode);
            stmt.setString(3, App.username); // game and player details
            stmt.executeUpdate();

            // update number of players in lobby
            stmt = conn.prepareStatement("UPDATE game "
                    + "SET player_count = player_count + 1 WHERE game_id = ?"); // +
            stmt.setString(1, gameCode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
