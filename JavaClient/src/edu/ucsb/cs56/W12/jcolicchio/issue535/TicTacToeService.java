package edu.ucsb.cs56.W12.jcolicchio.issue535;

/**
 * gictactoeservice allows clientconnect to communicate with tictactoe game
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class TicTacToeService extends TwoPlayerGameService {
    public TicTacToeGame gameData;

    public ClientConnect player1;
    public ClientConnect player2;

    public boolean gameStarted;

    public TicTacToeService(int ID) {
        super(ID);
        gameData = new TicTacToeGame();
        type = 1;
        name = "TicTacToe";
    }
    
    public void init() {
        gameData.init();
        broadcastData("INIT;");
    }

    public void playClient(ClientConnect client) {
        if(player1 == null) {
            player1 = client;
            gameData.player1 = client.client;
        } else if(player2 == null && player1 != client) {
            player2 = client;
            gameData.player2 = client.client;
            gameStarted = true;
            System.out.println("ready to play: "+player1.client.id+" vs "+player2.client.id);
            gameData.init();
        }

        updateAll();
    }

    //if a client was a player, spec him, and then probably stop the game
    public void specClient(ClientConnect client) {
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
    public void handleData(ClientConnect client, String string) {
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

        if(!gameStarted)
            return;
        System.out.println(gameData.turn+", "+client.client.id+", "+player1.client.id+":"+player2.client.id);
        if(gameData.turn == 1 && client != player1)
            return;
        if(gameData.turn == 2 && client != player2)
            return;
        if(string.indexOf("MOVE;") == 0) {
            if(gameData.winner != 0)
                return;
            System.out.println("got move command from "+client.client.id+": "+string);
            String[] data = string.substring(5).split(",");
            int X = Integer.parseInt(data[0]);
            int Y = Integer.parseInt(data[1]);

            if(gameData.grid[Y][X] != 0)
                return;

            gameData.grid[Y][X] = gameData.turn;
            broadcastData("MOVE[" + gameData.turn + "]" + X + "," + Y);
            if(gameData.checkWinner())
                broadcastData("WINNER;"+gameData.winner);
            gameData.turn = 3-gameData.turn;
        }
    }

    //this could be done better, just broadcast gameData.getGameState and have that function generate this:
    //wait but that isnt possible
    //sends the state of the game to a player
    public void sendGameState(ClientConnect client) {
        if(client == null)
            return;
        synchronized (client) {
            client.sendMessage(gameData.getState());
            String players = "PLAYERS;";
            if(gameData.player1 != null)
                players += gameData.player1.id;
            else
                players += "-1";
            players += ",";
            if(gameData.player2 != null)
                players += gameData.player2.id;
            else
                players += "-1";

            client.sendMessage(players);
        }
    }
}