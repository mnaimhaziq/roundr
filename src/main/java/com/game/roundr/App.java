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

    public static Server getServer() {
        return server;
    }

    public static void setServer(Server server) {
        App.server = server;
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement("UPDATE player SET ip_address = ? WHERE username = ?");
            stmt.setString(1, server.toString());
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Error in updating IP address in the database.");
            alert.showAndWait();
        }
    }


    public static Client getClient() {
        return client;
    }

    public static void setClient(Client client) {
        App.client = client;
        try {

            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement("UPDATE player SET ip_address = ? WHERE username = ?");
            stmt.setString(1, client.toString());
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Error in updating IP address in the database.");
            alert.showAndWait();
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        return FXMLLoader.load(App.class.getResource(fxml + ".fxml"));
    }

    public static void main(String[] args) {
        launch();
        int i = 1;
        while(i > 0){
//            System.out.println(clientHandlers);
            try {
                Thread.sleep(2000); // Sleep for 2 seconds (2000 milliseconds)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


}
