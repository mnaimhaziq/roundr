package com.game.roundr.lobby;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import com.game.roundr.App;
import com.game.roundr.DatabaseConnection;
import com.game.roundr.MainMenuController;
import com.game.roundr.game.MainGameAreaController;
import com.game.roundr.models.NavState;
import com.game.roundr.models.User;
import com.game.roundr.models.chat.Message;
import com.game.roundr.networking.Client;
import com.game.roundr.networking.ClientInt;
import com.game.roundr.networking.Server;
import com.game.roundr.networking.ServerInt;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class GameLobbyController implements Initializable {

    @FXML
    private MainMenuController mainMenuController;
    private ClientInt client;
    private ServerInt server;

    // @FXML
    // private void handleSendMessageButton() throws IOException {
    // // TO DO: display the message in the chatbox
    // }

    // @Override
    // public void initialize(URL location, ResourceBundle resources) {
    // Get message content from database
    // try {
    // ResultSet rs = new DatabaseConnection()
    // .getConnection()
    // .prepareStatement("SELECT * FROM chat;")
    // .executeQuery();
    // while (rs.next()) {
    // chatData.add(new Chat(
    // rs.getInt(1),
    // rs.getString(2),
    // rs.getInt(3),
    // rs.getInt(4),
    // rs.getInt(5),
    // rs.getInt(6),
    // rs.getInt(7)
    // ));
    // }
    // } catch (SQLException e) {
    // e.printStackTrace();
    // }

    // // Set the lobbies into the list
    // lobbyList.setItems(gameData);
    // // Set styling of the list cells
    // lobbyList.setCellFactory((ListView<Game> l) -> new LobbyCell());
    // throw new UnsupportedOperationException("Unimplemented method 'initialize'");
    // }

    // Generate lobby list cell nodes
    // private Node createGraphic(Game game) {
    // HBox hBox = new HBox(40.0);
    // hBox.setPrefHeight(60.0);
    // hBox.setAlignment(Pos.CENTER);

    // Circle circle = new Circle(15.0);
    // if (game.getNumOfPlayers() < game.getPlayerLimit()) {
    // circle.setFill(Color.web("#A1FF89"));
    // } else {
    // circle.setFill(Color.web("#FF7171"));
    // }

    // Text lobbyName = new Text("Player" + "'s Lobby");
    // lobbyName.setFont(new Font("Inter Bold", 18.0));
    // lobbyName.setWrappingWidth(320.0);

    // Text playerCount = new Text(game.getNumOfPlayers()
    // + "/"
    // + game.getPlayerLimit());
    // playerCount.setFont(new Font("Inter Bold", 18.0));

    // hBox.getChildren().addAll(circle, lobbyName, playerCount);
    // return hBox;
    // }

    // }

    private static final int MIN_USERS = 2; // default min users required to start
    private static final int ROOM_CAPACITY = 6; // default max room capacity

    private NavState state;

    // Server Chatbox
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
    private ArrayList<Label> listNicknameS;
    private ArrayList<Label> listReadyS;

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
    private ArrayList<Label> listNicknameC;
    private ArrayList<Label> listReadyC;

    // lobby
    @FXML
    private GridPane gridPaneUsers;
    @FXML
    private Button readyButton;
    @FXML
    private Circle readyCircle;
    @FXML
    private Button buttonStartGame;
    // @FXML
    // private TextArea textAreaChat;
    @FXML
    private TextField nameTextField = mainMenuController.getTextField();

    private int connectedUsers;

    private SimpleDateFormat tformatter;

    public void initialize(URL location, ResourceBundle resources) {

        this.state = NavState.MULTIPLAYER;
        this.chatS.setVisible(false);
        this.chatC.setVisible(false);

        this.tformatter = new SimpleDateFormat("[HH:mm:ss]");

        this.listNicknameS = new ArrayList<Label>();
        this.listNicknameC = new ArrayList<Label>();
        this.listReadyS = new ArrayList<Label>();
        this.listReadyC = new ArrayList<Label>();

        // To automatically resize and scroll to the latest chat (server)
        chatS.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                spChatS.setVvalue((Double) newValue);
            }
        });

        // To automatically resize and scroll to the latest chat (client)
        chatC.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                spChatC.setVvalue((Double) newValue);
            }
        });

        // popolate the GridPane with HBox and set them not visible
        for (int i = 0; i < ROOM_CAPACITY; i++) {

            // Add players to the GridPane (Client)
            int rowIndex = 0;
            int colIndex = 0;
            for (Label username : listNicknameC) {
                // Create an HBox for each player
                HBox hbox = new HBox();
                hbox.setSpacing(10);
                hbox.setPadding(new Insets(5));
                hbox.setVisible(false);

                // Create a label for player name
                username.setVisible(i == 0 ? false : true);
                // Label label = new Label(player);
                Circle readyCircle = new Circle(15.0);

                // Add label to the HBox
                hbox.getChildren().addAll(username, readyCircle);

                // Increment row and column indices
                colIndex++;
                if (colIndex >= 2) {
                    colIndex = 0;
                    rowIndex++;
                }

                ObservableList<Node> children = this.gridPaneUsers.getChildren();
                for (Node node : children) {
                    if (node instanceof Label) {
                        Label label = (Label) node;
                        // String text = label.getText();
                        // Perform operations on the label's text...
                        this.listNicknameC.add(label);
                    }
                }

                username = new Label("");
                username.setPrefSize(25, 25);
                username.setStyle("-fx-background-color: #D3D3D3");
                username.setVisible(i == 0 ? false : true);
                hbox.getChildren().add(username);
                this.listReadyC.add(username);

                // Add HBox to the GridPane
                this.gridPaneUsers.add(hbox, colIndex, rowIndex);
            }

            // Add players to the GridPane (Server)
            for (Label username : listNicknameS) {
                // Create an HBox for each player
                HBox hbox = new HBox();
                hbox.setSpacing(10);
                hbox.setPadding(new Insets(5));
                hbox.setVisible(false);

                // Create a label for player name
                username.setVisible(i == 0 ? false : true);
                // Label label = new Label(player);
                Circle readyCircle = new Circle(15.0);

                // Add label to the HBox
                hbox.getChildren().addAll(username, readyCircle);

                // Increment row and column indices
                colIndex++;
                if (colIndex >= 2) {
                    colIndex = 0;
                    rowIndex++;
                }
                // check if there is a Hbox or not in the gridpane
                ObservableList<Node> children = this.gridPaneUsers.getChildren();
                for (Node node : children) {
                    if (node instanceof Label) {
                        Label label = (Label) node;
                        this.listNicknameS.add(label);
                    }
                }

                username = new Label("");
                username.setPrefSize(25, 25);
                username.setStyle(i == 0 ? "-fx-background-color: #00FF00" : "-fx-background-color: #D3D3D3");
                username.setVisible(i == 0 ? false : true);
                hbox.getChildren().add(username);
                this.listReadyS.add(username);

                // Add HBox to the GridPane
                this.gridPaneUsers.add(hbox, colIndex, rowIndex);

            }
        }

        connectedUsers = 0;
    }

    // Generate random colors
    private String randomColors() {
        Random rand = new Random(System.currentTimeMillis());
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        // Change to hex code
        String hex = String.format("#%02X%02X%02X", r, g, b);
        return hex;

    }

    // Assign a color to a player once player join/create a lobby, not invoked yet
    private String assignColor() {
        String color = null;
        Connection conn = new DatabaseConnection().getConnection();

        try {
            // Statement to select the player_color column
            PreparedStatement selectStatement = conn.prepareStatement(
                    "SELECT pg.player_color FROM player_game pg JOIN player p ON pg.player_id = p.player_id WHERE p.username = ?");
            selectStatement.setString(1, App.username);

            // Execute the select query and retrieve the result
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                // Check if the player_color column is empty
                String playerColor = resultSet.getString("player_color");

                if (playerColor == null || playerColor.isEmpty()) {
                    // Generate a random color
                    color = randomColors();

                    // Try to insert the color into the database
                    PreparedStatement insertStatement = conn.prepareStatement(
                            "UPDATE player_game pg JOIN player p ON pg.player_id = p.player_id SET pg.player_color = ? WHERE p.username = ?");
                    insertStatement.setString(1, color);
                    insertStatement.setString(2, App.username);

                    // Execute the update statement
                    int rowsAffected = insertStatement.executeUpdate();

                    if (rowsAffected == 1) {
                        // Color successfully assigned
                        App.playerColor = color;
                    } else {
                        // Failed to assign color
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to assign color to the player.");
                        alert.showAndWait();
                    }
                } else {
                    // Player already has a color assigned
                    App.playerColor = playerColor;
                }
            } else {
                // Player not found
                Alert alert = new Alert(Alert.AlertType.WARNING, "Player not found.");
                alert.showAndWait();
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return color;
    }

    // Allow player to toggle the Not Ready button
    // By default the player is not ready
    @FXML
    public void toggleReady(ActionEvent event) {
        if (this.readyButton.getText().equalsIgnoreCase("Ready")) {
            this.readyButton.setText("Not ready");
            this.readyButton.setStyle("-fx-background-color: #D3D3D3");
            this.readyCircle.setFill(Color.web("#D3D3D3"));
            this.client.sendReady(false);
            this.updateReady(nameTextField.getText(), false);
        } else {
            this.readyButton.setText("Ready");
            this.readyButton.setStyle("-fx-background-color: #00FF00");
            this.readyCircle.setFill(Color.web("#00FF00"));
            this.client.sendReady(true);
            this.updateReady(this.nameTextField.getText(), true);
        }
    }

    // send message from Server
    @FXML
    public void handleSendMessageButtonS(ActionEvent event) throws IOException {
        String msg = this.textFieldChatS.getText();
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

            this.server.sendChatMessage(msg);
            this.textFieldChatS.setText("");
            this.textFieldChatS.clear();
        }

    }

    // send message from Server by clicking ENTER key
    @FXML
    private void enterChatHandleS(KeyEvent event) {
        if (this.state == NavState.MP_SERVER && event.getCode().equals(KeyCode.ENTER)) {
            String msg = this.textFieldChatS.getText();
            if (!msg.isEmpty() && !msg.isBlank())
                this.server.sendChatMessage(msg);
            this.textFieldChatS.setText("");
        }
    }

    // chat bubble for client (display from server)
    public static void addChatBubbleS(String msg, VBox vbox) {

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(msg);
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

            this.client.sendChatMessage(msg);
            this.textFieldChatC.setText("");
            this.textFieldChatC.clear();
        }
    }

    // send message from Client by clicking ENTER key
    @FXML
    public void enterChatHandleC(KeyEvent event) {
        if (this.state == NavState.MP_CLIENT && event.getCode().equals(KeyCode.ENTER)) {
            String msg = this.textFieldChatC.getText();
            if (!msg.isEmpty() && !msg.isBlank())
                this.client.sendChatMessage(msg);
            this.textFieldChatC.setText("");
        }
    }

    public String getCurrentTimestamp() {
        Date date = new Date(System.currentTimeMillis());
        String timestamp = this.tformatter.format(date);

        return timestamp;
    }

    public void switchToServerRoom() {
        this.chatS.setVisible(true);
        this.state = NavState.MP_SERVER;
    }

    public void switchToClientRoom() {
        this.chatC.setVisible(true);
        this.state = NavState.MP_CLIENT;
    }

    // chat bubble for server (display from client)
    public static void addChatBubbleC(String msgFromServer, VBox vBox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(msgFromServer);
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

    // method ni tak siap lagi
    public void addToChatBox(String txt) {
        // client
        if (this.state == NavState.MP_CLIENT) {
            if (chatS.getChildren().stream().anyMatch(node -> node instanceof HBox)) {
                // this.chatS.getChildren(Text).setText(txt);
                addChatBubbleC(txt, chatC);

            } else {
                this.textAreaChat.setText(this.textAreaChat.getText() + "\n" + text);
                // addChatBubbleS(txt, chatS);
            }
        }
        // server
        else if (this.state == NavState.MP_SERVER) {
            this.textAreaChat.setText(this.textAreaChat.getText() + "\n" + text);
            // addChatBubbleS(txt, chatS);
        }
    }

    // display the message in HBox
    public void addToTextArea(Message message) {
        this.addToChatBox(message.getTimestamp() + " " + message.getNickname() + ": " + message.getContent());
    }

    public void updateReady(String nickname, boolean ready) {
        if (this.state == NavState.MP_CLIENT) {
            for (int i = 0; i < this.gridPaneUsers.getChildren().size(); i++) {
                if (nickname.equals(this.listNicknameC.get(i).getText())) {
                    this.listReadyC.get(i)
                            .setStyle(ready ? "-fx-background-color: #81DB62" : "-fx-background-color: #DF4D4D");
                    break;
                }
            }
        } else if (this.state == NavState.MP_SERVER) {
            for (int i = 0; i < this.gridPaneUsers.getChildren().size(); i++) {
                if (nickname.equals(this.listNicknameS.get(i).getText())) {
                    this.listReadyC.get(i)
                            .setStyle(ready ? "-fx-background-color: #81DB62" : "-fx-background-color: #DF4D4D");
                    break;
                }
            }
        }
    }

    public void resetList() {
        if (this.state == NavState.MP_CLIENT) {
            Platform.runLater(() -> {
                for (int i = 0; i < ROOM_CAPACITY; i++) {
                    this.gridPaneUsers.getChildren().get(i).setVisible(false);
                    this.listNicknameC.get(i).setText("");
                    this.listReadyC.get(i).setStyle("-fx-background-color: #DF4D4D");
                }
            });
        } else if (this.state == NavState.MP_SERVER) {
            for (int i = 0; i < ROOM_CAPACITY; i++) {
                this.gridPaneUsers.getChildren().get(i).setVisible(false);
                this.listNicknameS.get(i).setText("");
                this.listReadyS.get(i).setStyle("-fx-background-color: #DF4D4D");
            }
        }
    }

    public void addUser(User u) {
        Platform.runLater(() -> {
            if (this.state == NavState.MP_CLIENT) {
                this.listNicknameC.get(this.connectedUsers).setText(u.getNickname());
                this.gridPaneUsers.getChildren().get(this.connectedUsers).setVisible(true);
                this.connectedUsers++;
            } else if (this.state == NavState.MP_SERVER) {
                this.listNicknameS.get(this.connectedUsers).setText(u.getNickname());
                this.gridPaneUsers.getChildren().get(this.connectedUsers).setVisible(true);
                this.connectedUsers++;

                this.buttonStartGame.setDisable(true); // when a new user connects it's always not ready
            }
        });

    }

    public void enableStartGame(boolean value) {
        this.buttonStartGame.setDisable(!value);
    }

    // public void removeUser(String nickname)
    // {
    // Platform.runLater(() -> {
    // boolean found = false;
    // if(this.state == NavState.MP_CLIENT)
    // {
    // // NB: we have to move by one position back every user, to fill the empty
    // space left by the removed one
    // for(int i = 1; i < this.connectedUsers; i++)
    // {
    // if(found)
    // {
    // // we move every entry up by 1, overriding the one to remove
    // this.listUsernameC.get(i - 1).setText(this.listUsernameC.get(i).getText());
    // this.listReadyC.get(i - 1).setStyle(this.listReadyC.get(i).getStyle());
    // this.listImagePlayer.get(i -
    // 1).setVisible(this.listImagePlayer.get(i).isVisible());
    // }
    // if(this.listUsernameC.get(i).getText().equals(nickname))
    // found = true;
    // }
    // // we hide the last entry
    // this.listViewUsersC.getItems().get(this.connectedUsers -
    // 1).setVisible(false);
    // this.listUsernameC.get(this.connectedUsers - 1).setText("");
    // this.listReadyC.get(this.connectedUsers - 1).setStyle("-fx-background-color:
    // red");
    // this.listImagePlayer.get(this.connectedUsers - 1).setVisible(false);
    // this.connectedUsers--;
    // }
    // else if(this.state == NavState.MP_SERVER)
    // {
    // // NB: we have to move by one position back every user, to fill the empty
    // space left by the removed one
    // for(int i = 1; i < this.connectedUsers; i++)
    // {
    // if(found)
    // {
    // // we move every entry up by 1, overriding the one to remove
    // this.listNicknameS.get(i - 1).setText(this.listNicknameS.get(i).getText());
    // this.listReadyS.get(i - 1).setStyle(this.listReadyS.get(i).getStyle());
    // }
    // if(this.listNicknameS.get(i).getText().equals(nickname))
    // found = true;
    // }
    // // we hide the last entry
    // this.listViewUsersS.getItems().get(this.connectedUsers -
    // 1).setVisible(false);
    // this.listNicknameS.get(this.connectedUsers - 1).setText("");
    // this.listReadyS.get(this.connectedUsers - 1).setStyle("-fx-background-color:
    // red");
    // this.connectedUsers--;

    // this.buttonStartGame.setDisable(!this.server.checkCanStartGame());
    // }
    // });
    // }
    @FXML
    public void handleLeaveLobbyButtonClick() throws IOException {
        // When a player clicks the leave lobby button
        // int playerCount = game.getNumOfPlayers();
        // int gameID = game.getGameID();
        // // Execute if there are still players in the lobby
        // if (playerCount > 0) {
        // try {
        // // Decrement the players count every time a player leaves
        // playerCount--;
        // // Update the player count according to the game ID in the database
        // Connection conn = new DatabaseConnection().getConnection();
        // PreparedStatement stmt = conn.prepareStatement("UPDATE game SET player_count
        // = ? WHERE game_id = ?");
        // stmt.setInt(1, playerCount);
        // stmt.setInt(2, gameID);
        // stmt.executeUpdate();
        // // System.out.println("Player left the lobby. Player count: " + playerCount);
        // } catch (SQLException e) {
        // // e.printStackTrace();
        // Alert alert = new Alert(Alert.AlertType.WARNING, "Error in updating player
        // count in the database.");
        // alert.showAndWait();
        // }
        // } else {
        // System.out.println("No players in the lobby.");
        if (App.server != null) {
            App.server.closeServer();
            App.server = null;
        }
        // Handle client disconnection
        else if (App.client != null) {
            App.client.closeClient();
            App.client = null;
        }
        App.setScene("MainMenu");
        decrementTemp();
    }

    @FXML
    private void handleReadyButton() throws IOException {
        App.setScene("game/MainGameArea");

    }

    private void decrementTemp() {
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE game SET player_count = player_count - 1 WHERE game_id = (SELECT MAX(game_id) FROM game)");
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
