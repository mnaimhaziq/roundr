
package com.game.roundr.lobby;

import com.game.roundr.App;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import com.game.roundr.networking.Client;
import com.game.roundr.networking.Server;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
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
        // Create the server when the button is clicked
        App.server = new Server(App.username);
        App.client = null;// Start the server
        App.setRole("Server");
        System.out.println(App.server);

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Add options to combo boxes
        ObservableList<Integer> ol = FXCollections.observableArrayList();
        for (int i = 0; i < 10; i++) {
            ol.add(i + 1);
        }
        roundsBox.getItems().addAll(ol);
        wordBox.getItems().addAll(ol);
        ol.clear();
        for (int i = 0; i < 60; i++) {
            ol.add(i + 1);
        }
        timeBox.getItems().addAll(ol);
        playerBox.getItems().addAll(2, 3, 4, 5, 6);
    }


}
