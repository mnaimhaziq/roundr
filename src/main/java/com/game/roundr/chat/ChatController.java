package com.game.roundr.chat;

import com.game.roundr.App;
import com.game.roundr.DatabaseConnection;
import com.game.roundr.models.Game;
import com.game.roundr.models.Message;
import com.game.roundr.models.State;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.ResourceBundle;

import com.game.roundr.network.Client;
import com.game.roundr.network.ClientHandler;
import com.game.roundr.network.ClientListener;
import com.game.roundr.network.Server;
import com.game.roundr.network.ServerListener;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ChatController implements Initializable {

    // Host Chatbox
    @FXML
    private VBox chatS;
    @FXML
    private ScrollPane spChatS;
    @FXML
    private TextField textFieldChatS;
    @FXML
    private Circle playerCircleS;
    @FXML
    private Button buttonChatSendS;

    // Client Chatbox
    @FXML
    private VBox chatC;
    @FXML
    private ScrollPane spChatC;
    @FXML
    private TextField textFieldChatC;
    @FXML
    private Circle playerCircleC;
    @FXML
    private Button buttonChatSendC;

    private SimpleDateFormat tformatter;
    private Server server;
    private ServerListener listenerS;
    private Client client;
    private ClientListener listenerC;
    private ClientHandler handlerC;
    private State state;

    // send message from Server
    @FXML
    public void handleSendMessageButtonS(ActionEvent event) throws IOException {
        // handleUserInput();
        String msg = this.textFieldChatS.getText();
        if (!msg.isEmpty() && !msg.isBlank()) {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);
            hBox.setPadding(new Insets(5, 5, 5, 10));

            Text t = new Text(msg);
            // t.setWrapText(true);
            TextFlow textFlow = new TextFlow(t);

            textFlow.setStyle("-fx-color: rgb(239,242,255)" +
                    "-fx-background-color: rgb(15,125,242)" +
                    "-fx-background-radius: 20px");

            textFlow.setPadding(new Insets(5, 10, 5, 10));
            t.setFill(Color.color(0.934, 0.945, 0.996));

            // Paint playerColor = getPlayerColorFromDatabase();
            // Circle circle = new Circle(10, playerColor)

            hBox.getChildren().add(textFlow);
            // chatS.getChildren().add(hBox);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    chatS.getChildren().add(hBox);
                }
            });

            this.textFieldChatS.setText("");
            this.textFieldChatS.clear();
        }
        listenerS.sendMessageToClient(msg);
        textFieldChatS.clear();
    }

    // // send message from Server by clicking ENTER key
    // @FXML
    // private void enterChatHandleS(KeyEvent event) {
    // if (event.getCode().equals(KeyCode.ENTER)) {
    // String msg = this.textFieldChatS.getText();
    // if (!msg.isEmpty() && !msg.isBlank())
    // this.listenerS.sendChatMessage(msg);
    // this.textFieldChatS.setText("");
    // }
    // }

    private String getPlayerColorFromDatabase() {
        String playerColor = null;
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT player_color FROM player_game WHERE player_game_id = ?");
            stmt.setInt(1, App.playerGameID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                playerColor = rs.getString("player_color");
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerColor;
    }

    // server receive message from client
    // chat bubble for client (display from server)
    public static void addChatBubbleS(Message msg, VBox vbox) {

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(); // pass the message later
        TextFlow textFlow = new TextFlow(text);

        textFlow.setStyle("-fx-background-color: rgb(1233,233,235)" +
                "-fx-background-radius: 20px");

        textFlow.setPadding(new Insets(5, 10, 5, 10));
        text.setFill(Color.color(0.934, 0.945, 0.996));

        hBox.getChildren().add(textFlow);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vbox.getChildren().add(hBox);
            }
        });
    }

    // client to server
    // send message from Client
    @FXML
    public void handleSendMessageButtonC(ActionEvent event) {
        String msg = this.textFieldChatC.getText();
        if (!msg.isEmpty() && !msg.isBlank()) {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);
            hBox.setPadding(new Insets(5, 5, 5, 10));

            Text t = new Text(msg);
            TextFlow textFlow = new TextFlow(t);

            textFlow.setStyle("-fx-color: rgb(239,242,255)" +
                    "-fx-background-color: rgb(15,125,242)" +
                    "-fx-background-radius: 20px");

            textFlow.setPadding(new Insets(5, 10, 5, 10));
            t.setFill(Color.color(0.934, 0.945, 0.996));

            hBox.getChildren().add(textFlow);
            chatS.getChildren().add(hBox);

            this.handlerC.sendMessageToServer(msg);
            this.textFieldChatC.setText("");
            this.textFieldChatC.clear();
        }
    }

    // // send message from Client by clicking ENTER key
    // @FXML
    // public void enterChatHandleC(KeyEvent event) {
    // if (this.state == NavState.MP_CLIENT &&
    // event.getCode().equals(KeyCode.ENTER)) {
    // String msg = this.textFieldChatC.getText();
    // if (!msg.isEmpty() && !msg.isBlank())
    // this.client.sendChatMessage(msg);
    // this.textFieldChatC.setText("");
    // }
    // }

    public String getCurrentTimestamp() {
        Date date = new Date(System.currentTimeMillis());
        String timestamp = this.tformatter.format(date);

        return timestamp;
    }

    // public void switchToServerRoom() {
    // this.chatS.setVisible(true);
    // this.state = NavState.MP_SERVER;
    // }

    // public void switchToClientRoom() {
    // this.chatC.setVisible(true);
    // this.state = NavState.MP_CLIENT;
    // }

    // from server to client
    // chat bubble for server (display from client)
    public static void addChatBubbleC(Message msg, VBox vBox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text();
        TextFlow textFlow = new TextFlow(text);

        textFlow.setStyle("-fx-background-color: rgb(1233,233,235)" +
                "-fx-background-radius: 20px");

        textFlow.setPadding(new Insets(5, 10, 5, 10));
        text.setFill(Color.color(0.934, 0.945, 0.996));

        hBox.getChildren().add(textFlow);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vBox.getChildren().add(hBox);
            }
        });
    }

    // public class DialogBox extends HBox {

    // private Label text;

    // public DialogBox(Label content, Circle circle) {
    // text = content;
    // playerCircleS = circle;

    // text.setWrapText(true);
    // playerCircleS.setFill(Color.web("#A1FF89"));
    // playerCircleS.setRadius(15.0);

    // this.setAlignment(Pos.TOP_RIGHT);
    // this.getChildren().addAll(text, playerCircleS);
    // }
    // }

    // private void flip() {
    // this.setAlignment(Pos.TOP_LEFT);
    // ObservableList<Node> tmp =
    // FXCollections.observableArrayList(this.getChildren());
    // FXCollections.reverse(tmp);
    // this.getChildren().setAll(tmp);
    // }

    // public static DialogBox getSenderDialog(Label l, Circle c) {
    // return new DialogBox(l, c);
    // }

    // public static DialogBox getReceiverDialog(Label content, Circle circle) {
    // var db = new DialogBox(content, circle);
    // db.flip();
    // return db;
    // }

    // // method ni tak siap lagi
    // public void addToChatBox(String txt) {
    // // client
    // if (this.state == NavState.MP_CLIENT) {
    // if (chatS.getChildren().stream().anyMatch(node -> node instanceof HBox)) {
    // // this.chatS.getChildren(Text).setText(txt);
    // // addChatBubbleC(txt, chatC);

    // } else {
    // this.chatS.setText(this.chatS.getChildren().getText() + "\n" + text);
    // // addChatBubbleS(txt, chatS);
    // }
    // }
    // // server
    // else if (this.state == NavState.MP_SERVER) {
    // this.textAreaChat.setText(this.textAreaChat.getText() + "\n" + text);
    // // addChatBubbleS(txt, chatS);
    // }
    // }
    // private String getResponse(String input) {
    // return ": " + input;
    // }

    // private void handleUserInput() {
    // Label senderText = new Label(textFieldChatS.getText());
    // Label receiverText = new Label(getResponse(textFieldChatC.getText()));

    // chatS.getChildren().addAll(
    // DialogBox.getSenderDialog(senderText, new Circle()),
    // DialogBox.getReceiverDialog(receiverText, new Circle()));
    // textFieldChatS.clear();
    // }

    // public void addToChatBox(String text)
    // {
    // // client
    // if(this.state == State.CLIENT)
    // {
    // if(this.chatC.getText().isEmpty())
    // this.chatC.setText(text);
    // else this.chatC.setText(this.chatC.getText() + "\n" + text);
    // }
    // // server
    // else if(this.state == State.SERVER)
    // {
    // this.chatS.setText(this.chatS.getText() + "\n" + text);
    // }
    // }

    // // display the message in HBox
    // public void addToChatBox(Message message) {
    // this.addToChatBox(message.getTimestamp() + " " + message.getSenderName() +
    // ": " + message.getContent());
    // // this.handleUserInput();
    // }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.chatS.setVisible(false);
        this.chatC.setVisible(false);

        this.tformatter = new SimpleDateFormat("[HH:mm:ss]");

        // this.listNicknameS = new ArrayList<Label>();
        // this.listNicknameC = new ArrayList<Label>();
        // this.listReadyS = new ArrayList<Label>();
        // this.listReadyC = new ArrayList<Label>();

        if (this.state == State.SERVER) {
            // To automatically resize and scroll to the latest chat (server)
            chatS.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    spChatS.setVvalue((Double) newValue);
                }
            });

            try {
                listenerS.receiveMessageFromClient(chatS);
                handleSendMessageButtonS(null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (this.state == State.CLIENT) {
            // To automatically resize and scroll to the latest chat (client)
            chatC.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    spChatC.setVvalue((Double) newValue);
                }
            });

            handlerC.receiveMessageFromServer(chatC);
            handleSendMessageButtonC(null);
        }

        throw new UnsupportedOperationException("Unimplemented method 'initialize'");
    }

}
