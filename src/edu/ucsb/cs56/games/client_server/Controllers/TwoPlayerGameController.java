package edu.ucsb.cs56.games.client_server.Controllers;

import java.util.ArrayList;

import edu.ucsb.cs56.games.client_server.Controllers.Network.ClientNetworkController;

/**
 * an abstract service classification for games which have two players
 * all two-player games should extend this
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public abstract class TwoPlayerGameController extends ChatController{
    public ClientNetworkController player1;
    public ClientNetworkController player2;
    
    public boolean gameStarted;
    
    public TwoPlayerGameController(int ID) {
        clients = new ArrayList<ClientNetworkController>();
        gameStarted = false;
        id = ID;
    }
    
    public abstract void init();
    
    public void updateAll() {
        synchronized (clients) {
            for(int i=0;i<clients.size();i++)
                sendGameState(clients.get(i));
        }
    }

    public void addClient(ClientNetworkController client) {
        synchronized (clients) {
            super.addClient(client);
            sendGameState(client);
        }
    }

    public abstract void playClient(ClientNetworkController client);

    public abstract void specClient(ClientNetworkController client);

    public void removeClient(ClientNetworkController client) {
        specClient(client);
        super.removeClient(client);
    }
    
    public void broadcastDate(String data) {
        synchronized (clients) {
            for(int i=0;i<clients.size();i++)
                clients.get(i).sendMessage(data);
        }
    }

    public abstract void sendGameState(ClientNetworkController client);
}
