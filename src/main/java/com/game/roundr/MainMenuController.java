package com.game.roundr;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class MainMenuController implements Initializable {

    @FXML
    private TextField nameTextField;

    @FXML
    private void handleCreateLobbyButtonClick() throws IOException {
        if (checkIsValidInputName()) {
            App.setScene("lobby/CreateLobby");
        }
    }

    @FXML
    private void handleJoinLobbyButtonClick() throws IOException {
        if (checkIsValidInputName()) {
            App.setScene("lobby/JoinLobby");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        nameTextField.setText(App.username);
    }

    private boolean checkIsValidInputName() {
        // Get value of text field
        String inputName = nameTextField.getText().trim();

        // Check if input is empty
        if (inputName.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter your name.");
            alert.showAndWait();
            return false;
        }
        
        // Check if username is set
        if (App.username.isEmpty()) {
            // Try insert in the db
            try {
                Connection conn = new DatabaseConnection().getConnection();
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO player(username) VALUES(?)");
                stmt.setString(1, inputName);
                stmt.executeUpdate();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "This name is already taken.");
                alert.showAndWait();
                return false;
            }
            App.username = inputName;
        // Check if the name is changed
        } else if (!App.username.equals(inputName)) {
            // Update the prior username to the new username
            try {
                Connection conn = new DatabaseConnection().getConnection();
                PreparedStatement stmt = conn.prepareStatement("UPDATE `player` SET `username` = ? WHERE `username` = ?");
                stmt.setString(1, inputName);
                stmt.setString(2, App.username);
                stmt.executeQuery();
                App.username = inputName;
            } catch (SQLException e) {
                return false;
            }
        }

        return true;
    }

}
