package com.game.roundr;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
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
        Pattern PATTERN_NICKNAME = Pattern.compile("^[A-Za-z0-9]{1,20}$");
        
        // Get value of text field
        String inputName = nameTextField.getText().trim();

        // Check if input is empty
        if (inputName.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter your name.");
            alert.showAndWait();
            return false;
        }
        
        // Validate username
        if (!PATTERN_NICKNAME.matcher(inputName).matches()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Invalid name, must be 1-20 long and alphanumeric.");
            alert.showAndWait();
            return false;
        }
        
        // Check if username is set
        if (App.username.isEmpty()) {
            // Try insert in the db
            try {
                Connection conn = new DatabaseConnection().getConnection();
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO player(username, ip_address) VALUES(?, ?)");
                stmt.setString(1, inputName);
                stmt.setString(2, InetAddress.getLocalHost().getHostAddress());
                stmt.executeUpdate();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "This name is already taken.");
                alert.showAndWait();
                return false;
            } catch (UnknownHostException e) {
                e.printStackTrace();
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
                stmt.executeUpdate();
                App.username = inputName;
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "This name is already taken.");
                alert.showAndWait();
                return false;
            }
        }

        return true;
    }

}
