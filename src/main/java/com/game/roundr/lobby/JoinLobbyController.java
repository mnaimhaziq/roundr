
package com.game.roundr.lobby;

import com.game.roundr.App;
import com.game.roundr.DatabaseConnection;
import com.game.roundr.models.Game;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.game.roundr.networking.Client;
import com.game.roundr.networking.Server;
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
    private ListView<Game> lobbyList;

    private Client client;
    private Server server;
    ObservableList<Game> gameData = FXCollections.observableArrayList();

    @FXML
    private void handleMainMenuButtonClick() throws IOException {

        App.setScene("MainMenu");
    }



    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get lobbies from database
        try {
            ResultSet rs = new DatabaseConnection()
                    .getConnection()
                    .prepareStatement("SELECT * FROM game LIMIT 10;")
                    .executeQuery();
            while (rs.next()) {
                gameData.add(new Game(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getInt(3),
                        rs.getInt(4),
                        rs.getInt(5),
                        rs.getInt(6),
                        rs.getInt(7)
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set the lobbies into the list
        lobbyList.setItems(gameData);
        // Set styling of the list cells
        lobbyList.setCellFactory((ListView<Game> l) -> new LobbyCell());
    }



    public void handleJoinLobbyButton() throws IOException{
        App.setRole("Client");
        createClient("localhost", 9001);
        App.setScene("lobby/GameLobby");

    }

    private void createClient(String serverAddress, int serverPort) {
        App.client = new Client( serverAddress, serverPort, App.username);
        App.server = null;
        System.out.println("Connected to server: " + serverAddress + ":" + serverPort);
        System.out.println("App.client: " + App.client);

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

        Text lobbyName = new Text("Player" + "'s Lobby");
        lobbyName.setFont(new Font("Inter Bold", 18.0));
        lobbyName.setWrappingWidth(320.0);

        Text playerCount = new Text(game.getNumOfPlayers()
                + "/"
                + game.getPlayerLimit());
        playerCount.setFont(new Font("Inter Bold", 18.0));

        hBox.getChildren().addAll(circle, lobbyName, playerCount);
        return hBox;
    }

}