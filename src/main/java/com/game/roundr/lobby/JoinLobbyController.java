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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
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
        // fetch the game code from the text field
        String gameCode = codeTextField.getText();

        // if the game exists, get the game IP address and connect to lobby
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM "
                    + "game WHERE game_id = ?");
            stmt.setString(1, gameCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) { 
                joinLobby(rs.getString("ip_address")); 
            }
            
            // close db resources
            conn.close();
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // list automatically shows any changes in games
        lobbyList.setItems(games);
        
        // get the list of lobbies from the database and set list styles
        getLobbies();
        lobbyList.setCellFactory((ListView<Game> l) -> new LobbyCell());
    }

    private class LobbyCell extends ListCell<Game> {

        @Override
        protected void updateItem(Game item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                setGraphic(createGraphic(item)); // cell content
            }
        }
    }

    private Node createGraphic(Game game) {
        // parent container
        HBox hBox = new HBox(40.0);
        hBox.setPrefHeight(60.0);
        hBox.setAlignment(Pos.CENTER);

        // availability indicator
        Circle circle = new Circle(15.0);
        circle.setStroke(Color.BLACK);
        circle.setStrokeType(StrokeType.INSIDE);
        if (game.getNumOfPlayers() < game.getPlayerLimit())
            circle.setFill(Color.web("#A1FF89"));
        else {
            circle.setFill(Color.web("#FF7171"));
        }

        // host name
        Text name = new Text(game.getHostName() + "'s Lobby");
        name.setFont(new Font("Inter Bold", 18.0));
        name.setWrappingWidth(320.0);

        // number of players in the game
        Text count = new Text(game.getNumOfPlayers() + "/"
                + game.getPlayerLimit());
        count.setFont(new Font("Inter Bold", 18.0));

        hBox.getChildren().addAll(circle, name, count);

        // click cell to try join server
        hBox.setOnMouseClicked((MouseEvent evt) -> {
            joinLobby(game.getIpAddress());
        });

        return hBox;
    }

    private void joinLobby(String gameAddress) {
        MainGameAreaController mgac = new MainGameAreaController();
        App.client = new Client(gameAddress, App.username, mgac);
        App.client.startClient();
    }
    
    private void getLobbies() {
        try(ResultSet rs = new DatabaseConnection().getConnection()
                    .prepareStatement("""
                        SELECT game.*, player.username AS `host_name` FROM game 
                        JOIN player_game ON game.game_id = player_game.game_id
                        JOIN player ON player.player_id = player_game.player_id 
                        AND player_game.is_host = '1' LIMIT 10;""")
                    .executeQuery()) {
            while (rs.next()) {
                games.add(new Game(
                        rs.getInt("game_id"),
                        rs.getString("game_status"),
                        rs.getInt("player_limit"),
                        rs.getInt("player_count"),
                        rs.getString("ip_address"),
                        rs.getString("host_name")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

}