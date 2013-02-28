package edu.ucsb.cs56.W12.jcolicchio.issue535;

/**
 * GomokuService is a server-side service that allows clients to send moves to the server's copy of a gomoku game
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class GomokuService extends TwoPlayerGameService {
    public GomokuGame gameData;

    public ClientConnect player1;
    public ClientConnect player2;

    public boolean gameStarted;

    /**
     * start gomoku service with id ID
     * @param ID id of service
     */
    public GomokuService(int ID) {
        super(ID);
        gameData = new GomokuGame();
        type = 2;
        name = "Gomoku";
    }

    /**
     * initialize with size
     * @param SIZE size of board to init with
     */
    public void init(int SIZE) {
        gameData.init(SIZE);
        broadcastData("SIZE;"+SIZE);
    }

    /**
     * default size is 9
     */
    public void init() {
        gameData.init(9);
        broadcastData("INIT;");
    }

    /**
     * add client to service
     * @param client clientconnect object to add
     */
    public void addClient(ClientConnect client) {
        super.addClient(client);
        client.sendMessage("SIZE;"+gameData.cells);
    }

    /**
     * set client as a player
     * @param client client to play
     */
    public void playClient(ClientConnect client) {
        if(player1 == null) {
            player1 = client;
            gameData.player1 = client.client;
        } else if(player2 == null && player1 != client) {
            player2 = client;
            gameData.player2 = client.client;
            gameStarted = true;
            System.out.println("ready to play: "+player1.client.id+" vs "+player2.client.id);
            gameData.init(gameData.cells);
        }

        updateAll();
    }

    //if a client was a player, spec him, and then probably stop the game

    /**
     * set playing client as spectator
     * @param client client to spectate
     */
    public void specClient(ClientConnect client) {
        if(player1 != client && player2 != client)
            return;
        if(player1 == client) {
            player1 = player2;
            gameData.player1 = gameData.player2;
            player2 = null;
            gameData.player2 = null;
            gameStarted = false;
            gameData.init(gameData.cells);
        }
        if(player2 == client) {
            player2 = null;
            gameData.player2 = null;
            gameStarted = false;
            gameData.init(gameData.cells);
        }

        updateAll();
    }

    //get move from player, if it's their turn

    /**
     * handle info from client
     * @param client the client sending the data
     * @param string data to handle
     */
    public void handleData(ClientConnect client, String string) {
        if(string.indexOf("PLAY;") == 0)
            playClient(client);
        else if(string.indexOf("SPEC;") == 0)
            specClient(client);
        else if(string.indexOf("SIZE;") == 0) {
            int size = Integer.parseInt(string.substring(5));
            init(size);
        } else if(string.indexOf("MSG;") == 0) {
            String message = string.substring(4);
            if(message.indexOf("/play")==0) {
                playClient(client);
            } else if(message.indexOf("/spec") == 0) {
                specClient(client);
            } else if(message.indexOf("/newgame") == 0) {
                if(client == player1 || client == player2)
                    init(gameData.cells);
            } else
                super.handleData(client, string);
        }

        if(!gameStarted || gameData.winner > 0)
            return;
        System.out.println(gameData.turn+", "+client.client.id+", "+player1.client.id+":"+player2.client.id);
        if(gameData.turn == 1 && client != player1)
            return;
        if(gameData.turn == 2 && client != player2)
            return;

        //this is an optional setting, some games may use it, eventually implement rule checkboxes
        //TODO: disallow moves that result in forming two 3's, (unblocked?), or 2 4's, blocked or unblocked
        if(string.indexOf("MOVE;") == 0) {
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

    //sends the state of the game to a player

    /**
     * send state of the game to client
     * @param client client to send state to
     */
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