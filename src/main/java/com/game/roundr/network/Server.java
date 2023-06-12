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
            // change view
            App.setScene("lobby/GameLobby");
            
            // create the main network thread
            listener = new ServerListener(PORT, this);
            new Thread(listener).start(); // network thread

            // generate random color
            String color = App.getHexColorCode();

            // adds host to the list
            App.glc.players.add(new Player(App.username, color));

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
            System.out.println("Server: The port and address is in use");
        } catch (IOException e) {
            System.out.println("Server: Failed to create server socket");
        }
    }

    public void closeServer() {
        App.glc.players.clear();
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

    public void sendEndGameRequest() {
        Message message = new Message(MessageType.END_GAME,
                App.username, "");
        sendMessage(message);
    }

    private void sendMessage(Message message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
