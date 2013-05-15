package edu.ucsb.cs56.games.client_server.Controllers;//Lobbyservice is a service that newly connecting players join by default
//it

import java.util.ArrayList;

import edu.ucsb.cs56.games.client_server.Controllers.Network.ClientNetworkController;
/**
 * Since all clients need an "active service" to be connected to, which listens to input, lobby service is a service that
 * extends chat service and does little else. quite honestly, it doesn't do much else besides extend chatservice, so
 * its existence is largely pointless
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class LobbyController extends ChatController {
    public LobbyController(int ID) {
        id = ID;
        type = 0;
        clients = new ArrayList<ClientNetworkController>();
        name = "Lobby";
    }
    
    @Override
    public void handleData(ClientNetworkController client, String data) {
        System.out.println("lobby "+id+" handled it");
        super.handleData(client, data);
        //for now, treat chat messages like client commands
    }
}