package edu.ucsb.cs56.games.client_server.Controllers;

import edu.ucsb.cs56.games.client_server.Controllers.Network.ClientNetworkController;
import edu.ucsb.cs56.games.client_server.Models.ChessModel;

/**
 * Chess service is run by the server and essentially connects the clients to the server's copy of the chess game
 * Can exist multiple chessservices on the server for different players, however the GUI does not as of yet provide
 * the ability for players to create new games. Players can type /new Chess to make a new game and join it, however
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class ChessController extends TwoPlayerGameController {
    public ChessModel gameData;

    public ClientNetworkController player1;
    public ClientNetworkController player2;

    public boolean gameStarted;

    /**
     * start the service with id number ID
     * @param ID id of the service
     */
    public ChessController(int ID) {
        super(ID);
        gameData = new ChessModel();
        type = 3;
        name = "Chess";
    }
    //TODO: service addClient shouldn't send the client any info

    /** initialize the service
     *
     */
    public void init() {
        gameData.init();
        broadcastData("INIT;");
    }

    /**
     * set client as a player of the game, if possible
     * @param client client to make a player
     */
    public void playClient(ClientNetworkController client) {
        if(player1 == null) {
            player1 = client;
            gameData.player1 = client.client;
        } else if(player2 == null && player1 != client) {
            player2 = client;
            gameData.player2 = client.client;
            gameStarted = true;
            System.out.println("ready to play: "+player1.client.getId()+" vs "+player2.client.getId());
            gameData.init();
        }

        updateAll();
    }

    //if a client was a player, spec him, and then probably stop the game

    /**
     * set playing client as spectator instead
     * @param client client to stop from playing
     */
    public void specClient(ClientNetworkController client) {
        if(player1 != client && player2 != client)
            return;
        if(player1 == client) {
            player1 = player2;
            gameData.player1 = gameData.player2;
            player2 = null;
            gameData.player2 = null;
            gameStarted = false;
            gameData.init();
        }
        if(player2 == client) {
            player2 = null;
            gameData.player2 = null;
            gameStarted = false;
            gameData.init();
        }

        updateAll();
    }

    //get move from player, if it's their turn

    /**
     * handle data from server
     * @param client the client sending the data
     * @param string data to handle
     */
    public void handleData(ClientNetworkController client, String string) {
        if(string.indexOf("PLAY;") == 0)
            playClient(client);
        else if(string.indexOf("SPEC;") == 0)
            specClient(client);
        else if(string.indexOf("MSG;") == 0) {
            String message = string.substring(4);
            if(message.indexOf("/play")==0) {
                playClient(client);
            } else if(message.indexOf("/spec") == 0) {
                specClient(client);
            } else if(message.indexOf("/newgame") == 0) {
                if(client == player1 || client == player2)
                    init();
            } else
                super.handleData(client, string);
        }
        //TODO: new standard for sending information about the service to the client

        if(!gameStarted || gameData.winner > 0)
            return;
        System.out.println(gameData.turn+", "+client.client.getId()+", "+player1.client.getId()+":"+player2.client.getId());
        if(gameData.turn == 1 && client != player1)
            return;
        if(gameData.turn == 2 && client != player2)
            return;
        if(string.indexOf("MOVE;") == 0) {
            System.out.println("got move command from "+client.client.getId()+": "+string);
            String[] data = string.substring(5).split(",");
            int X1 = Integer.parseInt(data[0]);
            int Y1 = Integer.parseInt(data[1]);
            int X2 = Integer.parseInt(data[2]);
            int Y2 = Integer.parseInt(data[3]);

            if(gameData.tryMove(X1,Y1,X2,Y2)) {
                System.out.println("move went through");
                char piece = gameData.grid[Y2][X2];
                //intercept castling and en passant here, don't send move[]
                if(Character.toLowerCase(piece) == 'p' && Y2 == (Character.isUpperCase(piece)?0:7)) {
                    broadcastData("PROMOTE["+gameData.turn+"]"+X1+","+Y1+","+X2+","+Y2);
                } else {
                    gameData.turn = 3-gameData.turn;
                    broadcastData("MOVE[" + gameData.turn + "]"+X1+","+Y1+","+X2+","+Y2);
                    if(gameData.checkWinner())
                        broadcastData("WINNER;"+gameData.winner);
                }
            }
        } else if(string.indexOf("PROMOTE;") == 0) {
            String[] data = string.substring(8).split(",");
            int X = Integer.parseInt(data[0]);
            int Y = Integer.parseInt(data[1]);
            char piece = data[2].charAt(0);
            gameData.grid[Y][X] = piece;
            gameData.turn = 3-gameData.turn;
            broadcastData("PROMOTE;"+X+","+Y+","+piece);
            if(gameData.checkWinner())
                broadcastData("WINNER;"+gameData.winner);
        }
    }

    //sends the state of the game to a player

    /**
     * send the client the state of the game
     * @param client client to send to
     */
    public void sendGameState(ClientNetworkController client) {
        if(client == null)
            return;
        synchronized (clients) {
            client.sendMessage(gameData.getState());
            String players = "PLAYERS;";
            if(gameData.player1 != null)
                players += gameData.player1.getId();
            else
                players += "-1";
            players += ",";
            if(gameData.player2 != null)
                players += gameData.player2.getId();
            else
                players += "-1";

            client.sendMessage(players);
        }
    }
}