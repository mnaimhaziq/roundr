package com.game.roundr.network;

import com.game.roundr.App;
import com.game.roundr.models.Message;
import com.game.roundr.models.MessageType;

import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.ArrayList;


public class ServerListener implements Runnable {

    private final Server server;
    private final ServerSocket serverSocket;
    private ArrayList<ObjectOutputStream> writers;
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
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    // public void sendMessageToClient(String msg) {

    //     try {
    //         output.writeObject(msg);
    //         String lineSeparator = System.getProperty("line.separator");
    //         byte[] lineSeparatorBytes = lineSeparator.getBytes();
    //         output.write(lineSeparatorBytes);
    //         output.flush();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         System.out.println("Error sending message to client.");
    //     }

    // }

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
        // public void receiveMessageFromClient(VBox vBox){
        //     try {
        //         Message msg = (Message) input.readObject();
        //         ChatController.addChatBubbleS(msg, vBox);

        //         } catch (Exception e) {
        //             e.printStackTrace();
        //             System.out.println("Error receiving message from client.");
                    
        //         }
        //     }
            
        
    

    public void sendChatMessage(String content)
    {
        Message msg = new Message(MessageType.CHAT, App.username, content);

        // send the chat message to everyone
        this.sendChatMessage(msg);
    }

    private void sendChatMessage(Message message)
    {
        // send the message to each user except the server
        for (ClientHandler handler : server.handlers) {
            try {
                handler.output.writeObject(message);
                handler.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendWordMessage(String content)
    {
        Message msg = new Message(MessageType.RANDOM_WORD, App.username, content);

        // send the chat message to everyone
        this.sendWordMessage(msg);
    }

    private void sendWordMessage(Message message)
    {
        // send the message to each user except the server
        for (ClientHandler handler : server.handlers) {
            try {
                handler.output.writeObject(message);
                handler.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPlayerScore(Map<String, Integer> playerScore)
    {
        Message msg = new Message(MessageType.PLAYER_SCORE, App.username, playerScore);

        // send the chat message to everyone
        this.sendPlayerScore(msg);
    }

    private void sendPlayerScore(Message message)
    {
        // send the message to each user except the server
        for (ClientHandler handler : server.handlers) {
            try {
                handler.output.writeObject(message);
                handler.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendShiftedTurn(String turn)
    {
        Message msg = new Message(MessageType.TURN, App.username, turn);

        // send the chat message to everyone
        this.sendShiftedTurn(msg);
    }

    private void sendShiftedTurn(Message message)
    {
        // send the message to each user except the server
        for (ClientHandler handler : server.handlers) {
            try {
                handler.output.writeObject(message);
                handler.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendEndGameRequest() {
        Message message = new Message(MessageType.END_GAME,
                App.username, "");
        this.sendEndGameRequest(message);
    }

    private void sendEndGameRequest(Message message)
    {
        // send the message to each user except the server
        for (ClientHandler handler : server.handlers) {
            try {
                handler.output.writeObject(message);
                handler.output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
