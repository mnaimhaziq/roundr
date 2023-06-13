package com.game.roundr;

import java.sql.*;

public class DatabaseConnection {

    public Connection databaseLink;

    public Connection getConnection() {
        String databaseName = "game";
        String databaseUser = "root";
        String databasePassword = "root";
        String url = "jdbc:mysql://localhost/" + databaseName;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            databaseLink = DriverManager.getConnection(url, databaseUser, databasePassword);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return databaseLink;
    }

    public static int getGameIdFromDB() throws SQLException {
        int gameId = 0;
        String ipAddress = getIpAddressFromDB();
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT game_id FROM game WHERE ip_address = ?");
            stmt.setString(1, ipAddress); // Assuming app.getUsername() returns the username to search for
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                gameId = rs.getInt("game_id");
            } else {
                throw new SQLException("Game ID not found for the given username.");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            // Handle SQLException
            e.printStackTrace();
        }

        return gameId;
    }

    public static String getIpAddressFromDB() throws SQLException {
        String ipAddress = null;
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT ip_address FROM player WHERE username = ?");
            stmt.setString(1, App.username); // Assuming app.getUsername() returns the username to search for
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ipAddress = rs.getString("ip_address");
            } else {
                throw new SQLException("IP Address not found for the given username.");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            // Handle SQLException
            e.printStackTrace();
        }

        return ipAddress;
    }

    public static int getPlayerIdFromDB() throws SQLException {
        int playerId = 0;
        try {
            Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT player_id FROM player WHERE username = ?");
            stmt.setString(1, App.username); // Assuming app.getUsername() returns the username to search for
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                playerId = rs.getInt("player_id");
            } else {
                throw new SQLException("Player ID not found for the given username.");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            // Handle SQLException
            e.printStackTrace();
        }

        return playerId;
    }


}
