package com.game.roundr.networking;

import com.game.roundr.App;
import com.game.roundr.models.User;
import com.game.roundr.models.chat.Message;
import com.game.roundr.models.chat.MessageType;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {
    private static final int PORT = 9001;
    private String nickname;
    private int minToStartGame = 2;
    private int maxNumUsers = 6;
    private ServerListener serverListener;

    private ArrayList<User> users;
    private ArrayList<ObjectOutputStream> writers;

    public Server(String nickname) {
        this.nickname = nickname;
        this.users = new ArrayList<>();
        User u = new User(nickname);
        this.users.add(u);
        this.writers = new ArrayList<>();
        this.writers.add(null);

        try {
            this.serverListener = new ServerListener(PORT);
            this.serverListener.start();
            App.setScene("lobby/GameLobby");
        } catch (IOException e) {
            System.out.println("Server: ServerSocket Creation Failed");
            if (e instanceof BindException) {
                System.out.println("Server: another socket is already bound to this address and port");
                try (final DatagramSocket socket = new DatagramSocket()) {
                    socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                    String privateIP = socket.getLocalAddress().getHostAddress();
                    System.out.println("Room creation failed. Another socket is already bound to " + privateIP + ":" + PORT);
                    App.setScene("lobby/CreateLobby");
                } catch (SocketException ex) {
                    throw new RuntimeException(ex);
                } catch (UnknownHostException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private class ServerListener extends Thread {
        private ServerSocket listener;

        public ServerListener(int port) throws IOException {
            this.listener = new ServerSocket(port);
            System.out.println("Server (" + this.getId() + "): listening for connections on port " + PORT);
        }

        @Override
        public void run() {
            try {
                while (!listener.isClosed()) {
                    Socket socket = this.listener.accept();
                    new ClientHandler(socket).start();
                }
            } catch (SocketException e) {
                if (e.getMessage().contains("accept failed"))
                    System.out.println("Server (" + this.getId() + "): stopped listening for connections");

                else {
                    System.out.println("You has been disconnected");
                }
            } catch (IOException e) {
                System.out.println("Server: error while trying to accept a new connection");
                e.printStackTrace();
            } finally {
                try {
                    this.listener.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void closeSocket() {
            try {
                this.listener.close();
            } catch (IOException e) {
                System.out.println("IOException while closing the socket");
                e.printStackTrace();
            }
        }
    }

    public class ClientHandler extends Thread {
        private Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;

        public ClientHandler(Socket socket) {
            System.out.println("Server (" + this.getId() + "): connection accepted");
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                this.input = new ObjectInputStream(this.socket.getInputStream());
                this.output = new ObjectOutputStream(this.socket.getOutputStream());

                while (this.socket.isConnected()) {
                    Message incomingMsg = (Message) this.input.readObject();
                    if (incomingMsg != null) {
                        System.out.println("Server (" + this.getId() + "): received " + incomingMsg.toString());
                        switch (incomingMsg.getMsgType()) {
                            case CONNECT: {
                                System.out.println("Server: connect message received");

                                Message mReply = new Message();

                                if (users.size() == maxNumUsers) {
                                    mReply.setMsgType(MessageType.CONNECT_FAILED);
                                    mReply.setNickname("");
                                    mReply.setContent("The room is full");
                                } else {
                                    // add user and writer to list
                                    User u = new User(incomingMsg.getNickname(), this.socket.getInetAddress());
                                    users.add(u);
                                    writers.add(this.output);

                                    // forward to other users the new user joined
                                    mReply.setMsgType(MessageType.USER_JOINED);
                                    mReply.setNickname(incomingMsg.getNickname());
                                    forwardMessage(mReply);

                                    // create OK message, containing the updated user list
                                    mReply.setMsgType(MessageType.CONNECT_OK);
                                    mReply.setNickname(nickname);
                                    mReply.setContent(getUserList());

                                    // add the message to the chat textArea
                                    System.out.println(mReply.getTimestamp() + " " + incomingMsg.getNickname() + " has joined the room");
                                }

                                this.output.writeObject(mReply);

                                break;
                            }

                            case DISCONNECT: {
                                // add the message to the chat textArea
                                System.out.println(incomingMsg.getTimestamp() + " " + incomingMsg.getNickname() + " has left the room");

                                // forward disconnection to others
                                forwardMessage(incomingMsg);

                                // remove user and writer from the list
                                for (int i = 1; i < users.size(); i++) {
                                    if (users.get(i).getNickname().equals(incomingMsg.getNickname())) {
                                        users.remove(i);
                                        writers.remove(i);
                                        break;
                                    }
                                }
                                // close the connection
                                this.socket.close();

                                break;
                            }
                            default: {
                                System.out.println("Server: received unknown message type: " + incomingMsg.toString());
                                break;
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                // "Connection reset" when the other endpoint disconnects
                if (e.getMessage().contains("Connection reset"))
                    System.out.println("Stream closed");
                    // "java.net.SocketException: Socket closed" - received DISCONNECT
                else if (e.getMessage().contains("Socket closed"))
                    System.out.println("Socket closed");
                else
                    e.printStackTrace();
            } catch (IOException e) {
                // IOException is thrown when the socket is closed
                if (e.getMessage().contains("Socket closed"))
                    System.out.println("Socket closed");
                else
                    e.printStackTrace();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (input != null)
                        input.close();
                    if (output != null)
                        output.close();
                    this.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendMessage(Message message) {
        // send the message to each user except the server
        for (int i = 1; i < this.users.size(); i++) {
            try {
                this.writers.get(i).writeObject(message);
            } catch (IOException e) {
                System.out.println("IOException while trying to send a message to the client");
                e.printStackTrace();
            }
        }
    }

    public void CloseServer() {
        Message msg = new Message(MessageType.DISCONNECT, this.nickname, "Server room closed");

        // send the message to each user except the server (NB: it's not a normal sendMessage)
        for (int i = 1; i < this.users.size(); i++) {
            msg.setNickname(this.users.get(i).getNickname());
            try {
                this.writers.get(i).writeObject(msg);
            } catch (IOException e) {
                // remove the writer at index i?
                e.printStackTrace();
            }
        }

        // close the socket
        this.serverListener.closeSocket();
    }

    private void forwardMessage(Message msg) {
        // forward the message to each connected client, except the one that sent the message first
        for (int i = 1; i < this.users.size(); i++) {
            if (!msg.getNickname().equals(this.users.get(i).getNickname())) {
                try {
                    this.writers.get(i).writeObject(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getUserList() {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < this.users.size(); i++) {
            User u = this.users.get(i);
            list.append(u.getNickname()).append(",").append(u.isReady());
            if (i != this.users.size() - 1) {
                list.append(";");
            }
        }

        return list.toString();
    }
}
