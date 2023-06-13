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
    
    public TextField getTextField() {
        return nameTextField;
    }

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
        Pattern PATTERN_NICKNAME = Pattern.compile("^[A-Za-z0-9]{3,20}$");

        // Get value of text field
        String inputName = nameTextField.getText().trim();

        // Check if input is empty
        if (inputName.isBlank()) {
            App.showAlert(Alert.AlertType.WARNING, "Empty Name", "Please enter your name.");
            return false;
        }

        // Validate username input
        if (!PATTERN_NICKNAME.matcher(inputName).matches()) {
            App.showAlert(Alert.AlertType.WARNING, "Invalid Name", "Must be 3-20 long and alphanumeric.");
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
                
                // close db resources
                conn.close();
                stmt.close();
            } catch (SQLException e) {
                App.showAlert(Alert.AlertType.WARNING, "Invalid Name", "This name is already taken.");
                return false;
            } catch (UnknownHostException e) {
                App.showAlert(Alert.AlertType.ERROR, "An Error has Occured", e.getMessage());
                return false;
            }
            App.username = inputName;
        } else if (!App.username.equals(inputName)) { // name change
            // Update the prior username to the new username
            try {
                Connection conn = new DatabaseConnection().getConnection();
                PreparedStatement stmt = conn.prepareStatement("UPDATE player SET `username` = ? WHERE `username` = ?");
                stmt.setString(1, inputName);
                stmt.setString(2, App.username);
                stmt.executeUpdate();
                
                // close db resources
                conn.close();
                stmt.close();
            } catch (SQLException e) {
                App.showAlert(Alert.AlertType.WARNING, "Invalid Name", "This name is already taken.");
                return false;
            }
        }
        App.username = inputName;
        return true;
    }

}
