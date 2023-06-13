package com.game.roundr.network;

import com.game.roundr.chat.ChatController;
import com.game.roundr.models.Message;
import com.game.roundr.models.MessageType;

import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerListener implements Runnable {

    private final Server server;
    private final ServerSocket serverSocket;
    protected ObjectOutputStream output;
    protected ObjectInputStream input;
    private ChatController chat;

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
                // Message message = new Message(MessageType.CHAT, server.hostUsername, "");
                // if(chat.handleSendMessageButtonS(null)){
                // chat.sendMessageToClient(message);
            }
        } catch (SocketException e) {
            System.out.println("Server: Server socket closed");
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

    public void sendEndGameRequestToClients() {
        Message message = new Message(MessageType.END_GAME, server.hostUsername, "");
        broadcastMessageToClients(message);
    }

    public void broadcastMessageToClients(Message message) {
        // Send the message to all connected clients
        for (ClientHandler handler : server.handlers) {
            try {
                handler.output.writeObject(message);
                handler.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // private void sendMessage(Message message)
    // {
    // try {
    // this.output.writeObject(message);
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }

    // public void sendChatMessage(String content)
    // {
    // Message msg = new Message(MessageType.CHAT, this.chat.getCurrentTimestamp(),
    // content);

    // // send the message
    // this.sendMessage(msg);

    // // add the message to the textArea
    // this.chat.addToChatBox(msg);(msg);
    // }

    public void sendMessageToClient(String msg) {

        try {
            output.writeObject(msg);
            String lineSeparator = System.getProperty("line.separator");
            byte[] lineSeparatorBytes = lineSeparator.getBytes();
            output.write(lineSeparatorBytes);
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending message to client.");
        }

    }

    // public void receiveMessageFromClient(VBox vBox){
    //     new Thread(new Runnable(){
    //         @Override
    //         public void run(){
                
    //             while (!serverSocket.isClosed()) {
    //                 try {
    //             Object msg = input.readObject();
    //             ChatController.addChatBubbleS(msg, vBox);
                

    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 System.out.println("Error receiving message from client.");
    //                 break;
    //             }
    //         }
    //         }
    //     }).start();
    // }
        public void receiveMessageFromClient(VBox vBox){
            try {
                Message msg = (Message) input.readObject();
                ChatController.addChatBubbleS(msg, vBox);

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error receiving message from client.");
                    
                }
            }
            
        
    

}
