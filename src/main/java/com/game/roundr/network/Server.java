package com.game.roundr.network;

import com.game.roundr.App;
import com.game.roundr.DatabaseConnection;
import com.game.roundr.models.Config;
import com.game.roundr.models.Player;
import com.game.roundr.models.Message;
import com.game.roundr.models.MessageType;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import javafx.scene.control.Alert;

public class Server {

    private static final int PORT = 9001;
    public ServerListener listener;
    protected int gameId;
    protected final Config config;
    protected ArrayList<ClientHandler> handlers = new ArrayList<>(); // Runnables
    protected ObjectOutputStream output;

    public Server(Config config) {
        this.config = config;
    }

    public void startServer() {
        try {
            // create the main network thread
            listener = new ServerListener(PORT, this);
            new Thread(listener).start(); // network thread

            // change view
            App.setScene("lobby/GameLobby");
            
            // generate random color
            String color = App.getHexColorCode();
            
            // adds host to the list
            App.glc.addPlayer(App.username, color);

            // update database table
            try {
                Connection conn = new DatabaseConnection().getConnection();

                // create game entry in the database
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO game"
                        + "(turn_rounds, "
                        + "turn_time_limit, "
                        + "word_length, "
                        + "player_limit, "
                        + "player_count, "
                        + "ip_address) "
                        + "VALUES(?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, config.numOfRounds);
                stmt.setInt(2, config.turnTimeLimit);
                stmt.setInt(3, config.wordLength);
                stmt.setInt(4, config.maxPlayers);
                stmt.setInt(5, 1);
                stmt.setString(6,
                        InetAddress.getLocalHost().getHostAddress());
                stmt.executeUpdate();

                // get the game id
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    this.gameId = generatedKeys.getInt(1);
                }

                // set the lobby name and lobby code
                App.glc.SetLobbyInfo(App.username, "" + gameId);

                // create player_game entry for host
                stmt = conn.prepareStatement("INSERT INTO player_game(game_id,"
                        + "player_id, is_host, player_color, final_score) "
                        + "SELECT MAX(game.game_id), player.player_id, 1, ?, 0"
                        + " FROM game "
                        + "JOIN player ON game.ip_address = player.ip_address "
                        + "WHERE game.ip_address = ? "
                        + "AND player.username = ?;");
                stmt.setString(1, color);
                stmt.setString(2,
                        InetAddress.getLocalHost().getHostAddress());
                stmt.setString(3, App.username);
                stmt.executeUpdate();

                // close db resources
                conn.close();
                stmt.close();
                generatedKeys.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (BindException e) {
            App.showAlert(Alert.AlertType.WARNING, "Cannot Create Lobby", "The port and address is in use");
        } catch (IOException e) {
            App.showAlert(Alert.AlertType.WARNING, "Cannot Create Lobby", "Failed to create server socket");
        }
    }

    public void closeServer() {
        App.glc.clearPlayers();
        Message msg = new Message(MessageType.DISCONNECT,
                App.username, "Server closed");

        // send server shutdown message to all connected players
        while (!handlers.isEmpty()) {
            try {
                handlers.get(0).output.writeObject(msg);
                handlers.get(0).closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // remove game details
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM player_game WHERE `game_id` = ?;");
            stmt.setInt(1, gameId);
            stmt.executeUpdate();

            stmt = conn.prepareStatement(
                    "DELETE FROM game WHERE `game_id` = ?;");
            stmt.setInt(1, gameId);
            stmt.executeUpdate();

            // close db resources
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // close server socket
        listener.closeServerSocket();
        App.server = null;
    }

    // build player list to send to other players
    public String getPlayerList() {
        StringBuilder list = new StringBuilder();

        ArrayList<Player> players = getPlayers();

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            list.append(player.getUsername())
                    .append(",")
                    .append(player.getColor())
                    .append(",")
                    .append(player.isReady());

            if (i != players.size() - 1) {
                list.append(";");
            }
        }

        return list.toString();
    }

    // get the list of players from database
    public ArrayList<Player> getPlayers() {
        ArrayList<Player> list = new ArrayList();
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT `player_game`.*, "
                    + "player.username FROM player_game JOIN player "
                    + "ON player.player_id = player_game.player_id");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Player(rs.getString("username"),
                        rs.getString("player_color"),
                        rs.getString("status").equals("ready")));
            }

            // close db resources
            conn.close();
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // general method to send msg to all clients
    public void sendMessage(Message msg) {
        for (ClientHandler handler : handlers) {
            try {
                handler.output.writeObject(msg);
                handler.output.flush();
            } catch (IOException e) {
                System.out.println("Server: Error while sending message to clients");
            }
        }
    }

    public void sendReady(String ready) throws IOException {
        Message msg = new Message(MessageType.READY, App.username, ready);

        // update database
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement("UPDATE player_game SET status = "
                    + "? WHERE player_id = (SELECT player_id FROM player WHERE username=?)");
            stmt.setString(1, msg.getContent());
            stmt.setString(2, msg.getSenderName());
            stmt.executeUpdate();

            // close db resources
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // update player inside server list
        App.glc.updatePlayer(msg);

        // forward message to other players
        sendMessage(msg);

        // if all ready, then start
        if (App.glc.isAllReady()) {
            App.setScene("game/MainGameArea");
        }
    }

    public void sendEndGameRequest() {
        Message msg = new Message(MessageType.END_GAME, App.username, "");
        sendMessage(msg);
    }

    public void sendShiftedTurn(String turn) {
        Message msg = new Message(MessageType.TURN, App.username, turn);
        sendMessage(msg);
    }

    public void sendPlayerScore(Map<String, Integer> scores) {
        Message msg = new Message(MessageType.PLAYER_SCORE, App.username, scores);
        sendMessage(msg);
    }

    public void sendChatMessage(String content) {
        Message msg = new Message(MessageType.CHAT, App.username, content);
        App.mgac.addToTextArea(msg);
        sendMessage(msg);
    }

    public void sendWordMessage(String content) {
        Message msg = new Message(MessageType.RANDOM_WORD, App.username, content);
        sendMessage(msg);
    }

}
