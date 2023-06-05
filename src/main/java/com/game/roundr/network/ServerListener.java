package com.game.roundr.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerListener implements Runnable {

    private final Server server;
    private final ServerSocket serverSocket;

    public ServerListener(int port, Server server) throws IOException {
        this.server = server;
        this.serverSocket = new ServerSocket(port);
        System.out.println("Server: Listening for connections on port " + port);
    }

    @Override
    public void run() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Server: A player is connected");
                // create a new thread to handle the player connection
                new Thread(new ClientHandler(socket, server)).start();
            }
        } catch (IOException e) {
            System.out.println("Server: Unable to accept the new connection");
        } finally {
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
