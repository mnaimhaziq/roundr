package com.game.roundr.lobby;

import com.game.roundr.App;
import com.game.roundr.models.Message;
import com.game.roundr.models.Player;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;

public class GameLobbyController implements Initializable {

    @FXML
    private TextField gameCode;
    
    @FXML
    private Button readyButton;

    @FXML
    private ListView<Player> playerList;
    
    public ObservableList<Player> players = FXCollections.observableArrayList();

    @FXML
    private TextField sendMessageInput;
    @FXML
    private TextArea textAreaChat;



    public void HandleMessageInput() {

        String messageChat =sendMessageInput.getText();

        if (!messageChat.isEmpty()) {
            Message message = new Message();
            message.setSenderName("me"); // Set the sender name as desired
            message.setContent(messageChat);

            addToTextArea(message); // Add the message to the chat area
        }
        if(App.server != null){
            App.server.listener.sendChatMessage(messageChat);

        }else{
            App.client.listener.sendChatMessage(messageChat);
        }
        sendMessageInput.clear();
    }

    public void onEnter(){
        sendMessageInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                HandleMessageInput();
            }
        });
    }

    public void addToTextArea(Message message)
    {

        this.addToTextArea(message.getSenderName() + ": " + message.getContent());
    }
    public void addToTextArea(String text){
// client
        if(App.client != null)
        {
            if(this.textAreaChat.getText().isEmpty())
                this.textAreaChat.setText(text);
            else this.textAreaChat.setText(this.textAreaChat.getText() + "\n" + text);
        }
        // server
        else if(App.server != null)
        {
            this.textAreaChat.setText(this.textAreaChat.getText() + "\n" + text);
        }
    }
    
    @FXML
    private void handleLeaveButtonClick() throws IOException {
        // handle disconnections by role
        if (App.server != null) {
            App.server.closeServer();
            App.server = null;
        } else if (App.client != null) {
            App.client.closeClient();
            App.client = null;
        }
        
        // return to main menu
        App.setScene("MainMenu");
    }

    @FXML
    private void handleReadyButtonClick() throws IOException {
//        if (readyButton.getText().equals("Ready")) {
//            readyButton.setText("Not Ready");
//            App.client.sendReady("ready");
//        } else {
//            readyButton.setText("Ready");
//            App.client.sendReady("not_ready");
//        }
        App.setScene("game/MainGameArea");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        App.glc = this;
        playerList.setItems(players);
        readyButton.setText("Ready");
        playerList.setCellFactory((ListView<Player> l) -> new PlayerCell());
    }

    private class PlayerCell extends ListCell<Player> {

        {
            setStyle("-fx-padding: 0px"); // removes default paddings
        }

        @Override
        protected void updateItem(Player item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                setGraphic(createGraphic(item)); // cell contents
            }
        }

    }

    private Node createGraphic(Player player) {
        HBox hBox = new HBox(20.0);
        hBox.setPadding(new Insets(0, 40, 0, 40));
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPrefHeight(50.0);
        hBox.setStyle("-fx-background-color: " + player.getColor() + ";");

        Circle circle = new Circle(10.0);
        circle.setStroke(Color.BLACK);
        circle.setStrokeType(StrokeType.INSIDE);
        if (player.isReady()) {
            circle.setFill(Color.BLACK);
        } else {
            circle.setFill(Color.WHITE);
        }

        Label label = new Label(player.getUsername());
        label.setFont(new Font("Inter Bold", 18.0));
        label.setStyle("-fx-opacity: 1.0;");

        hBox.getChildren().addAll(circle, label); // holds the nodes

        return hBox;
    }
    
    public void startGame() throws IOException {
        App.setScene("game/MainGameArea");
    }
    
    public void clearPlayers() {
        Platform.runLater(() -> {
            players.clear();
        });
    }
    
    public void updatePlayers(Message msg) {
        Platform.runLater(() -> {
            players.clear();
            ArrayList<Player> ls = App.client.extractPlayerList(msg.getContent());
            players.addAll(ls);
        });
    }

}
