package com.game.roundr;

import com.game.roundr.networking.Client;
import com.game.roundr.networking.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class App extends Application {

    private static Scene scene;
    public static String username = "";

    public static String playerRole = "";
    public static Server server;
    public static Client client;

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
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM player WHERE username = ?");
            stmt.setString(1, username);
            stmt.executeUpdate();
            System.exit(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Use to change the scene
    public static void setScene(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }


    public static void setRole(String role){
        playerRole = role;
        System.out.println("player role: " + playerRole);
    }


    private static Parent loadFXML(String fxml) throws IOException {
        return FXMLLoader.load(App.class.getResource(fxml + ".fxml"));
    }

    public static void main(String[] args) {
        launch();
    }


}
