package com.game.roundr;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class MainMenuController {

    @FXML
    private TextField inputNameTextField;

    public void handleCreateLobbyButtonClick() throws IOException {
        if (checkIsValidInputName())
            App.setScene("lobby/CreateLobby");
    }

    public void handleJoinLobbyButtonClick() throws IOException {
        if (checkIsValidInputName())
            App.setScene("lobby/JoinLobby");
    }

    private boolean checkIsValidInputName() {
        // Get the value from the text field
        String inputName = inputNameTextField.getText().trim();

        // Check if the input is empty
        if (inputName.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter your name.");
            alert.showAndWait();
            return false;
        }

        // Check if the input already exists
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
            stmt.setString(1, inputName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "This name is already taken.");
                alert.showAndWait();
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
        
        return true;
    }

}
