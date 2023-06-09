package com.game.roundr;

import com.game.roundr.game.MainGameAreaController;
import com.game.roundr.lobby.GameLobbyController;
import com.game.roundr.network.Client;
import com.game.roundr.network.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class App extends Application {

    private static Scene scene;
    public static String username = "";
    public static Server server;
    public static Client client;
    public static GameLobbyController glc;
    public static MainGameAreaController mainGameAreaController;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("MainMenu"));
        stage.setResizable(false);
        stage.setTitle("Rounder");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (server != null) {
            server.closeServer();
        }
        if (client != null) {
            client.closeClient();
        }
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM player WHERE username = ?");
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }

    /* ------------------------------ Utility methods ------------------------------ */
    // Use to change the scene
    public static void setScene(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        return FXMLLoader.load(App.class.getResource(fxml + ".fxml"));
    }

    // Use to generate random color hex codes
    public static String getHexColorCode() {
        // Colour in the pastel range (120-230)
        Random random = new Random();
        int r = random.nextInt(111) + 120;
        int g = random.nextInt(111) + 120;
        int b = random.nextInt(111) + 120;
        return String.format("#%02X%02X%02X", r, g, b);
    }

    public static void showAlert(Alert.AlertType type, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /* ------------------------------ Utility methods ------------------------------ */
}
