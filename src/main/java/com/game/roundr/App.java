package com.game.roundr;

import com.game.roundr.network.Client;
import com.game.roundr.network.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    private static Scene scene;
    public static String username = "";
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
        if (server != null) {
            server.closeServer();
        }
        if (client != null) {
            client.closeClient();
        }
    }
    
    // Use to change the scene
    public static void setScene(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        return FXMLLoader.load(App.class.getResource(fxml + ".fxml"));
    }

    public static void main(String[] args) {
        launch();
    }

}
