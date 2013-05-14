package edu.ucsb.cs56.games.client_server;

import java.util.ArrayList;

/**
 * an abstract service classification for games which have two players
 * all two-player games should extend this
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public abstract class TwoPlayerGameService extends ChatService{
    public ClientConnect player1;
    public ClientConnect player2;
    
    public boolean gameStarted;
    
    public TwoPlayerGameService(int ID) {
        clients = new ArrayList<ClientConnect>();
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

    public void addClient(ClientConnect client) {
        synchronized (clients) {
            super.addClient(client);
            sendGameState(client);
        }
    }

    public abstract void playClient(ClientConnect client);

    public abstract void specClient(ClientConnect client);

    public void removeClient(ClientConnect client) {
        specClient(client);
        super.removeClient(client);
    }
    
    public void broadcastDate(String data) {
        synchronized (clients) {
            for(int i=0;i<clients.size();i++)
                clients.get(i).sendMessage(data);
        }
    }

    public abstract void sendGameState(ClientConnect client);
}
