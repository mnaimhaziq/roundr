package com.game.roundr.lobby;

import com.game.roundr.App;
import com.game.roundr.models.Config;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import com.game.roundr.network.Server;
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

    public void handleMainMenuButtonClick() throws IOException {
        App.setScene("MainMenu");
    }

    @FXML
    private void handleCreateButtonClick() throws IOException {
        App.server = new Server(new Config(
                roundsBox.getValue(), 
                timeBox.getValue(), 
                wordBox.getValue(), 
                playerBox.getValue()));
        App.server.startServer();
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
