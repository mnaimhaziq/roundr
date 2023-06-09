package com.game.roundr.game;

import com.game.roundr.App;
import com.game.roundr.DatabaseConnection;
import com.game.roundr.models.Message;
import com.game.roundr.models.Player;
import com.game.roundr.network.Client;
import com.game.roundr.network.ClientListener;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;

public class MainGameAreaController{

    @FXML
    private Label roundLabel;
    @FXML
    private Label playerLabel;
    @FXML
    private Label randomWord;

    @FXML
    private TextField submitText;

    @FXML
    private Label timeLimitLabel;

    @FXML
    public Button endGameButton;

    @FXML
    private Button leaveGameButton;

    @FXML
    private Button submitButton;
    @FXML
    private ListView playerNameList;
    @FXML
    private ListView scoreList;
    @FXML
    private TextField sendMessageInput;
    @FXML
    private TextArea textAreaChat;

    private int roundCount = 1;
    private int roundLimit;
    private int timeLimit;
    private int wordLength;
    private int playerCount; // Turn order of players
    private int playerLimit; // Total number of players
    private int gameId;
    private int roundId;
    private int playerId;
    private Map<String, Integer> playerScore;
    private long startTime;
    public Timeline timer;
    private String turn = App.username;

    //multiplayer stuff

    // Constructor
    public MainGameAreaController() {
        // Initialize the timer
        this.timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            // Code to update the timer label or perform actions every second
        }));
        this.timer.setCycleCount(Timeline.INDEFINITE); // Set the cycle count or use a specific value

//        if (!connect()) initializeServer();
//
//        thread = new Thread(this, "MainGameAreaController");
//        thread.start();

    }

    // Setter method for MainGameAreaController instance
    public void setMainGameAreaController(Timeline timer) {
        this.timer = timer;
    }

    // Getter for the timer
    public Timeline getTimer() {
        return timer;
    }
    public void pauseTimer(){
        timer.pause();
    }

    public void initialize() {

        App.mainGameAreaController = this;

        try {
            Connection conn = new DatabaseConnection().getConnection();

            // Get game information from database
            PreparedStatement stmt = conn.prepareStatement("SELECT game.game_id, game.turn_rounds, game.turn_time_limit," +
                    " game.word_length, game.player_limit, game.player_count, player.username \n" +
                    "FROM game\n" +
                    "JOIN player_game ON game.game_id = player_game.game_id\n" +
                    "JOIN player ON player.player_id = player_game.player_id");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                // Set initial values for round count, time limit, and word length
                roundLimit = rs.getInt("game.turn_rounds");
                timeLimit = rs.getInt("game.turn_time_limit");
                wordLength = rs.getInt("game.word_length");
                playerLimit = rs.getInt("game.player_limit");
                playerCount = rs.getInt("game.player_count");
                App.username = rs.getString("player.username");
                gameId = rs.getInt("game.game_id");

            }

            // Close the result set, statement, and connection
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Initialize Player - Score Map
        playerScore = new HashMap<>();

        // Initialize score
//        for (int i=1; i <= playerLimit; i++) {
//            playerScore.put(Integer.toString(i), 0);
//        }

        if(App.server != null){
            List<Player> playerServer = App.server.getPlayers();
            for (Player player : playerServer) {
                String playerName = player.getUsername();
                playerScore.put(playerName, 0);
            }
            handlePassScore();
        }

        //render in-match scoreboard
        renderLiveScoreboard();

        // Update UI labels with initial values
        updateLabels();

        // Handle submit button click action
        submitButton.setOnAction(event -> {
            clickSubmitButton();
        });

        // Handle end game button
        endGameButton.setOnAction(event -> {
            if (App.client != null) {
                handleEndGamePopup();
                App.client.sendEndGameRequest();
            } else if (App.server != null) {
                handleEndGamePopup();
                App.server.sendEndGameRequest();
            }
            handleEndGameButton();
        });

        // Start the first player's turn
        updateRoundTable();
        startPlayerTurn();
    }

    int getPlayers(){
        return playerLimit;
    }

    private double calculateScore(){
        // score = configured time limit - time taken by the player to write the correct word
        long currentTime = System.currentTimeMillis();
        long timeTaken = (currentTime - startTime) / 1000; // Convert to seconds
        return timeLimit - timeTaken;
    }

    // Handle submit button
    private void clickSubmitButton(){
        if(isMatched()){
            System.out.println("Point Up");
            try {
                handlePlayerTurnEnd();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Try Again");
        }
    }

    // Check if matched?
    private boolean isMatched(){
        if(submitText.getText().equals(randomWord.getText())){
            return true;
        } else {
            return false;
        }
    }

    //render in-match scoreboard
    void renderLiveScoreboard(){
        // clear score in scoreboard
        playerNameList.getItems().clear();
        scoreList.getItems().clear();
        // add new score in scoreboard
        playerNameList.getItems().addAll(new ArrayList(playerScore.keySet()));
        scoreList.getItems().addAll(new ArrayList(playerScore.values()));
    };

    private void updateLabels() {
        handleShiftedTurn();
        roundLabel.setText("ROUND " + roundCount);
        playerLabel.setText("Player " + turn + "'s turn");
        timeLimitLabel.setText("Time Limit: " + timeLimit + " seconds");
    }

    private String getRandomWord(){
        try {
            // Create URL object with the API endpoint
            URL url = new URL("https://random-word-api.vercel.app/api?words=1&length=" + wordLength + "");

            // Create HttpURLConnection object and open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set request method to GET
            connection.setRequestMethod("GET");

            // Get the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Create BufferedReader to read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                // Read the response line by line
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the JSON response using Gson
                Gson gson = new Gson();
                String[] words = gson.fromJson(response.toString(), String[].class);

                // Extract the word from the array
                String word = words[0];

                return word;
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void handleGeneratedWord() {

        String generatedWord = getRandomWord();

        if (!generatedWord.isEmpty()) {
            Message messageGeneratedWord = new Message();
            messageGeneratedWord.setSenderName("me"); // Set the sender name as desired
            messageGeneratedWord.setContent(generatedWord);

            generateWordPass(messageGeneratedWord); // Add the message to the chat area
        }
        if(App.server != null){
            App.server.listener.sendWordMessage(generatedWord);
        }else{
            App.client.listener.sendWordMessage(generatedWord);
        }
//        sendMessageInput.clear();
    }

    public void generateWordPass(Message message)
    {
//        this.generateWordPass(message.getContent());
        Platform.runLater(
                new Runnable() {
                    @Override
                    public void run() {
                        generateWordPass(message.getContent());
                    }
                }
        );
    }

    public void generateWordPass(String generatedWord){
        // client
        if(App.client != null)
        {
            randomWord.setText(generatedWord);
        }
        // server
        else if(App.server != null)
        {
            randomWord.setText(generatedWord);
        }
    }

    private void startPlayerTurn() {
        // Logic to start the current player's turn
        System.out.println("Player " + turn + "'s turn");

        if(App.server != null){
            // shift turn
            turn = App.server.getPlayers().get(playerCount-1).getUsername();
        }

        // update players turn
        updateLabels();

        // random word generator by the server
        if(App.server != null) {
            handleGeneratedWord();
        }

        //reload live scoreboard
        renderLiveScoreboard();

        // Start the timer for the player's turn
        startTimer();
    }

    public void handlePassScore() {

            Message messageGeneratedWord = new Message();
            messageGeneratedWord.setSenderName("me"); // Set the sender name as desired
            messageGeneratedWord.setPlayerScore(playerScore);
            passedScorePass(messageGeneratedWord); // Add the message to the chat area

//        if(turn == App.username){
            if(App.server != null){
                App.server.listener.sendPlayerScore(playerScore);
            }else{
                App.client.listener.sendPlayerScore(playerScore);
            }
//        }
//        sendMessageInput.clear();
    }

    public void passedScorePass(Message message)
    {
//        this.generateWordPass(message.getContent());
        Platform.runLater(
                new Runnable() {
                    @Override
                    public void run() {
                        passedScorePass(message.getPLayerScore());
                    }
                }
        );
    }

    public void passedScorePass(Map<String, Integer> passedPlayerScore){
        // client
        if(App.client != null)
        {
            this.playerScore = passedPlayerScore;
            renderLiveScoreboard();
//            timer.stop();
            // Start the next player's turn
//            startPlayerTurn();
        }
        // server
        else if(App.server != null)
        {
            this.playerScore = passedPlayerScore;
            renderLiveScoreboard();
//            timer.stop();
            // Start the next player's turn
//            startPlayerTurn();
        }
    }

    public void handleShiftedTurn() {

        Message messageTurn = new Message();
        messageTurn.setSenderName("me"); // Set the sender name as desired
        messageTurn.setContent(turn);
        passedShiftedTurn(messageTurn); // Add the message to the chat area

            if(App.server != null){
                App.server.listener.sendShiftedTurn(turn);
            }else{
//                App.client.listener.sendShiftedTurn(turn);
            }
//        sendMessageInput.clear();
    }

    public void passedShiftedTurn(Message message)
    {
//        this.generateWordPass(message.getContent());
        Platform.runLater(
                new Runnable() {
                    @Override
                    public void run() {
                        passedShiftedTurn(message.getContent());
                    }
                }
        );
    }

    public void passedShiftedTurn(String turn){
        // client
        if(App.client != null)
        {
            this.turn = turn;
        }
        // server
        else if(App.server != null)
        {
            this.turn = turn;
        }
    }

    public void handleEndGamePopup() {

        Message messagePopup = new Message();
        messagePopup.setSenderName("me"); // Set the sender name as desired
        passedEndGamePopup(messagePopup); // Add the message to the chat area

        if(App.server != null){
            App.server.listener.sendEndGameRequest();
        }else{
            App.client.listener.sendEndGameRequest();
        }

    }

    public void passedEndGamePopup(Message message) {

        Platform.runLater(
                new Runnable() {
                    @Override
                    public void run() {
                        passedEndGamePopup(message.getContent());
                    }
                }
        );

    }

    public void passedEndGamePopup(String content) {

        if (App.server != null) {
            handleEndGameButton();
        } else{
            handleEndGameButton();
        }

    }

    void startTimer() {
        AtomicInteger remainingTime = new AtomicInteger(timeLimit);

        // Update the UI with the remaining time
        timeLimitLabel.setText("Time Limit: " + remainingTime + " seconds");

        // Create a timeline for the timer
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingTime.getAndDecrement();
            timeLimitLabel.setText("Time Limit: " + remainingTime + " seconds");

            if (remainingTime.get() <= 0) {
                // Time is up, end the player's turn
//                stopTimer();
                try {
                    handlePlayerTurnEnd();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }));

        timer.setCycleCount(timeLimit);
        timer.play();
        startTime = System.currentTimeMillis();
    }

    void stopTimer() {
        if (timer != null) {
            timer.stop();
            System.out.println("Score: " + calculateScore());
            //increment score
            playerScore.put(App.username, playerScore.get(App.username)+(int)calculateScore());
            //reload scoreboard
            renderLiveScoreboard();
            handlePassScore();
            }
    }

    private void handlePlayerTurnEnd() throws IOException {
        // calculate score and update game state

        // Stop the timer
        stopTimer();

        // Increment the currentPlayer for the next turn
        playerCount++;

        if (roundCount == roundLimit && playerCount > playerLimit) {
            // All players have finished their turns and all rounds have finished
            endGame();

        } else if (playerCount > playerLimit){
            // All players have finished their turns
            playerCount = 1;
            updateRoundTable(); // Update the round table with the current round information
            startNextRound();
        } else {
            // Start the next player's turn
            startPlayerTurn();
        }

        // Update the database with turn data
        try {
            Connection conn = new DatabaseConnection().getConnection();

            // Get round_id from database
            PreparedStatement statement = conn.prepareStatement("SELECT round.round_id, player_game.player_id " +
                    "FROM round " +
                    "JOIN player_game " +
                    "ON round.game_id = player_game.game_id");

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                roundId = rs.getInt("round.round_id");
                playerId = rs.getInt("player_game.player_id");
            }

            // Insert the turn data into the database
            String query = "INSERT INTO turn (round_id, player_id, words, time_taken, score) " +
                    "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, roundId);
            stmt.setInt(2, playerId);
            stmt.setString(3, submitText.getText());
            stmt.setLong(4, (System.currentTimeMillis() - startTime) / 1000);
            stmt.setDouble(5, calculateScore());
            stmt.executeUpdate();

            // Close the statement and connection
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateRoundTable() {
        try {
            Connection conn = new DatabaseConnection().getConnection();

            // Insert the round data into the database
            String query = "INSERT INTO round (game_id, round_number) " +
                    "VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, gameId); // Replace 'gameId' with the actual game ID
            stmt.setInt(2, roundCount);
            stmt.executeUpdate();

            // Close the statement and connection
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void startNextRound() {
        // Logic to start the next round
        roundCount++;
        System.out.println("Round " + roundCount + " started!");

        // ... codes

        updateLabels(); // Update the labels to reflect the new round

        // Start the first player's turn for the new round
        startPlayerTurn();
    }

    private void endGame() throws IOException {
        // calculate final score
        System.out.println("Game ended!");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Scoreboard.fxml"));
            Parent toScoreboard = loader.load();
            ScoreboardController toScoreboardController = loader.getController();

            // Pass the playerScore data to the ScoreboardController
            toScoreboardController.initData(playerScore);

            // Assuming you have a reference to the current Scene
            Scene currentScene = playerNameList.getScene();

            // Replace the root node of the current Scene with the Scoreboard view
            currentScene.setRoot(toScoreboard);

        } catch (IOException e) {
            e.printStackTrace();
        }

//        App.setScene("game/Scoreboard");
    }

    @FXML
    public void handleEndGameButton() {
        // Pause the timer
        timer.pause();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EndGamePopup.fxml"));
            Parent popupRoot = loader.load();
            EndGamePopupController popupController = loader.getController();

            // Pass the timer and resume function to the popup controller
            popupController.initData(timer, new Runnable() {
                @Override
                public void run() {
                    resumeTimer();
                }
            });

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(endGameButton.getScene().getWindow());
            popupStage.setScene(new Scene(popupRoot));
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void resumeTimer() {
        timer.play();
    }

    @FXML
    private void handleLeaveGameButton() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LeaveGamePopup.fxml"));
            Parent popupRoot = loader.load();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(leaveGameButton.getScene().getWindow());
            popupStage.setScene(new Scene(popupRoot));
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CHAT FUNCTIONALITIES
    public void HandleMessageInput() {

        String messageChat = sendMessageInput.getText();

        if (!messageChat.isEmpty()) {
            Message message = new Message();
            message.setSenderName("me"); // Set the sender name as desired
            message.setContent(messageChat);

            addToTextArea(message); // Add the message to the chat area
        }
        if(App.server != null){
            App.server.listener.sendChatMessage(messageChat);

        }else{
            App.client.listener.sendChatMessage(messageChat);
        }
        sendMessageInput.clear();
    }

    public void onEnter(){
        sendMessageInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                HandleMessageInput();
            }
        });
    }

    public void addToTextArea(Message message)
    {

        this.addToTextArea(message.getSenderName() + ": " + message.getContent());
    }
    public void addToTextArea(String text){
        // client
        if(App.client != null)
        {
            if(this.textAreaChat.getText().isEmpty())
                this.textAreaChat.setText(text);
            else this.textAreaChat.setText(this.textAreaChat.getText() + "\n" + text);
        }
        // server
        else if(App.server != null)
        {
            this.textAreaChat.setText(this.textAreaChat.getText() + "\n" + text);
        }
    }

//    @Override
//    public void run() {
//        while (true) {
//            tick();
//
//            if (!circle && !accepted) {
//                listenForServerRequest();
//            }
//        }
//    }
//
//    private void tick() {
//        if (errors >= 10) unableToCommunicateWithOpponent = true;
//
//        if (!yourTurn && !unableToCommunicateWithOpponent) {
//            try {
//                int space = dis.readInt();
//                if (circle) spaces[space] = "X";
//                else spaces[space] = "O";
//                checkForEnemyWin();
//                checkForTie();
//                yourTurn = true;
//            } catch (IOException e) {
//                e.printStackTrace();
//                errors++;
//            }
//        }
//    }
//
//    private void listenForServerRequest() {
//        Socket socket = null;
//        try {
//            socket = serverSocket.accept();
//            dos = new DataOutputStream(socket.getOutputStream());
//            dis = new DataInputStream(socket.getInputStream());
//            accepted = true;
//            System.out.println("CLIENT HAS REQUESTED TO JOIN, AND WE HAVE ACCEPTED");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private boolean connect() {
//        try {
//            socket = new Socket(ip, port);
//            dos = new DataOutputStream(socket.getOutputStream());
//            dis = new DataInputStream(socket.getInputStream());
//            accepted = true;
//        } catch (IOException e) {
//            System.out.println("Unable to connect to the address: " + ip + ":" + port + " | Starting a server");
//            return false;
//        }
//        System.out.println("Successfully connected to the server.");
//        return true;
//    }
//
//    private void initializeServer() {
//        try {
//            serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        yourTurn = true;
//        circle = false;
//    }
//
//    private void render() {
//        if (unableToCommunicateWithOpponent) {
//            System.out.println("Render: Unable to communicate");
//            return;
//        }
//
//        if (accepted) {
//            if (circle) {
//                System.out.println("Render: a server");
//            } else {
//                System.out.println("Render: a client");
//            }
//
//            if (won || enemyWon) {
//                if (won) {
//                    System.out.println("Render: won");
//                } else if (enemyWon) {
//                    System.out.println("Render: enemy won");
//                }
//            }
//            if (tie) {
//                System.out.println("Render: tie");
//            }
//        } else {
//            System.out.println("Render: not accepted");
//        }
//
//    }
}