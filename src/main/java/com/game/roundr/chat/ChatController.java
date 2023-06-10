package com.game.roundr.chat;
// package com.game.roundr.lobby;

// import com.game.roundr.App;
// import com.game.roundr.DatabaseConnection;
// import com.game.roundr.models.Game;
// import com.game.roundr.models.PlayerStatus;

// import java.io.IOException;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.SQLException;
// import java.util.Random;

// import com.game.roundr.networking.Client;
// import com.game.roundr.networking.Server;
// import javafx.fxml.FXML;
// import javafx.scene.control.Alert;
// import javafx.scene.layout.GridPane;

// public class GameLobbyController {

//     // When a player clicks the leave lobby button
//     // @FXML
//     // public void handleLeaveLobbyButtonClick() throws IOException {
//         // int playerCount = game.getNumOfPlayers();
//         // int gameID = game.getGameID();
//         // // Execute if there are still players in the lobby 
//         // if (playerCount > 0) {
//         //     try {
//         //         // Decrement the players count every time a player leaves
//         //         playerCount--;
//         //         // Update the player count according to the game ID in the database
//         //         Connection conn = new DatabaseConnection().getConnection();
//         //         PreparedStatement stmt = conn.prepareStatement("UPDATE game SET player_count = ? WHERE game_id = ?");
//         //         stmt.setInt(1, playerCount);
//         //         stmt.setInt(2, gameID);
//         //         stmt.executeUpdate();
//         //         // System.out.println("Player left the lobby. Player count: " + playerCount);
//         //     } catch (SQLException e) {
//         //         // e.printStackTrace();
//         //         Alert alert = new Alert(Alert.AlertType.WARNING, "Error in updating player count in the database.");
//         //         alert.showAndWait();
//         //     }
//         // } else {
//         //     System.out.println("No players in the lobby.");
//         // }
        
//     //     // Disconnect the server or client based on the role
//     //     System.out.println("client" + App.client);
//     //     if (App.playerRole.equals("Server")) {
//     //         App.server.CloseServer();
//     //         App.server = null;
//     //         App.setScene("MainMenu");
//     //     }

//     //     else if (App.playerRole.equals("Client")) {
//     //         App.client.CloseClient();
//     //         App.client = null;
//     //         App.setScene("MainMenu");
//     //     }
//     // }

//     // @FXML
//     // private void handleReadyButton() throws IOException {
//         // PlayerStatus playerStatus = ;
//         // int playerGameID = ;
//         // if () {
//         //     try {
                
//         //         // Update the player status to "ready" according to the player game ID in the database
//         //         Connection conn = new DatabaseConnection().getConnection();
//         //         PreparedStatement stmt = conn.prepareStatement("UPDATE game SET status = ? WHERE player_game_id = ?");
//         //         stmt.setString(1, "ready");
//         //         stmt.setInt(2, playerGameID);
//         //         stmt.executeUpdate();

//         //     } catch (SQLException e) {
//         //         // e.printStackTrace();
//         //         Alert alert = new Alert(Alert.AlertType.WARNING, "Error in updating player status in the database.");
//         //         alert.showAndWait();
//         //     }
//         // } else {
//         //     System.out.println("No players in the lobby.");
//         // }

//     //     App.setScene("game/MainGameArea");
//     // }

// //     public void generateColor (){
// //         // GridPane grid = new GridPane();
// //         Random rand = new Random(System.currentTimeMillis());
// //         try {
// //             for (int i = 0; i < 6; i++) {
// //                 int red = rand.nextInt(255);
// //                 int green = rand.nextInt(255);
// //                 int blue = rand.nextInt(255);
    
// //                 Text text = new Text(x, y, "Java 7 Recipes");
    
// //                 int rot = rand.nextInt(360);
// //                 text.setFill(Color.rgb(red, green, blue, .99));
// //                 text.setRotate(rot);
// //                 root.getChildren().add(text);
// //             }
// //         } catch (Exception e) {
// //             // TODO: handle exception
        
        
// //     }

// // }


// }
