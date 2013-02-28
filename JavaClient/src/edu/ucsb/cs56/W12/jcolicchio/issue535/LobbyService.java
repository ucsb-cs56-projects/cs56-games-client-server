package edu.ucsb.cs56.W12.jcolicchio.issue535;//Lobbyservice is a service that newly connecting players join by default
//it

import java.util.ArrayList;
/**
 * Since all clients need an "active service" to be connected to, which listens to input, lobby service is a service that
 * extends chat service and does little else. quite honestly, it doesn't do much else besides extend chatservice, so
 * its existence is largely pointless
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class LobbyService extends ChatService {
    public LobbyService(int ID) {
        id = ID;
        type = 0;
        clients = new ArrayList<ClientConnect>();
        name = "Lobby";
    }
    
    @Override
    public void handleData(ClientConnect client, String data) {
        System.out.println("lobby "+id+" handled it");
        super.handleData(client, data);
        //for now, treat chat messages like client commands
    }
}