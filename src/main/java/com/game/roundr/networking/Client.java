package com.game.roundr.networking;

import com.game.roundr.App;
import com.game.roundr.models.User;
import com.game.roundr.models.chat.Message;
import com.game.roundr.models.chat.MessageType;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Client {

    private ClientListener clientListener;
    private String nickname;
    private OutputStream os;
    private ObjectOutputStream output;
    private InputStream is;
    private ObjectInputStream input;

    public  Client(String address, int port, String nickname){
        this.nickname = nickname;
        this.clientListener = new ClientListener(address, port);
        this.clientListener.start();
    }

    private class  ClientListener extends Thread{
        private Socket socket;
        private String address;
        private int port;

        public ClientListener(String address, int port){
            this.address = address;
            this.port = port;
        }

        @Override
        public void run(){
            System.out.println("Client: running. Nickname: " + nickname);
            try {
                this.socket= new Socket(address, port);

                os = this.socket.getOutputStream();
                output = new ObjectOutputStream(os);
                is = this.socket.getInputStream();
                input = new ObjectInputStream(is);

                // send CONNECT message
                Message msg = new Message(MessageType.CONNECT, nickname, "");
                output.writeObject(msg);

                while (this.socket.isConnected())
                {
                    Message incomingMsg = (Message)  input.readObject();
                    if(incomingMsg != null)
                    {
                        System.out.println("Client (" + this.getId() + "): received " + incomingMsg.toString()); // test
                        switch (incomingMsg.getMsgType())
                        {
                            case CONNECT_FAILED :
                            {
                                System.out.println("connecting failed");
                                break;
                            }
                            case CONNECT_OK:
                            {
                                App.setScene("lobby/GameLobby");
                                System.out.println("Extract User List: " + extractUserList(incomingMsg.getContent()));
                                // add the message to the chat textArea
                                System.out.println(nickname + " has joined the room");

                                break;

                            }
                            case USER_JOINED:
                            {
                                // add the message to the chat textArea
                                System.out.println( incomingMsg.getNickname() + " has joined the room");;


                                break;
                            }
                            case DISCONNECT:
                            {
                                // the room has been closed (connection lost from the server)
                                if(incomingMsg.getNickname().equals(nickname))
                                {
                                    // switch view


                                    // close connection(?)

                                    // show alert
                                    System.out.println( "Disconnected from server. Message: " + incomingMsg.getContent());
                                    App.setScene("MainMenu");
                                }
                                // another user disconnected
                                else
                                {
                                    // add the message to the chat textArea
                                    System.out.println( incomingMsg.getNickname() + " has left the room");


                                }

                                break;
                            }
                            default:
                            {
                                System.out.println("Client: received unknow message type: " + incomingMsg.toString());
                                break;
                            }
                        }
                    }
                }
            } catch(SocketException e) {
                System.out.println("Socket exception");
                if(e instanceof ConnectException)
                {
                    System.out.println( "Connection failed" + e.getMessage());
                }
                else if(e.getMessage().equals("Connection reset"))
                {
                    System.out.println("Stream closed");
                }
                else e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Stream closed");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    public void CloseClient()
    {
        Message msg = new Message(MessageType.DISCONNECT, this.nickname, "");

        // send disconnect message
        this.sendMessage(msg);
    }

    private void sendMessage(Message message)
    {
        try {
            this.output.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private List<User> extractUserList(String s)
    {
        List<User> list = new ArrayList<User>();

        String[] sTmp = s.split(";");
        for(int i = 0; i < sTmp.length; i++)
        {
            String[] sNickReady = sTmp[i].split(",");
            User u = new User(sNickReady[0]);
            u.setReady(Boolean.parseBoolean(sNickReady[1]));
            list.add(u);
        }

        return list;
    }


}
