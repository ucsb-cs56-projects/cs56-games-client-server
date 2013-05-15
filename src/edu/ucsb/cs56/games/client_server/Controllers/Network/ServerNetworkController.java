package edu.ucsb.cs56.games.client_server.Controllers.Network;

import edu.ucsb.cs56.games.client_server.Models.ClientModel;

/**
 * server connect was a specific dummy user that joined every service when it was made, and allowed the server to "speak"
 * by sending messages from id 0, which was always server
 * since i added a new standard of communication, SMSG, for specifically that purpose, the serverconnect class is largely
 * useless, and is not used at all in javaserver
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class ServerNetworkController extends ClientNetworkController {
    public ServerNetworkController(int id) {
        super(null);
        client = new ClientModel(id, "Server", 0);
        System.out.println("server has client: "+client.getName());
    }

    @Override
    public void sendMessage(String string) {
        //System.out.println("server got msg: "+string);
        if(string.indexOf("PMSG[") == 0) {
            String[] data = string.substring(5).split("]");
            int id = Integer.parseInt(data[0]);
            String msg = string.substring(5+data[0].length()+1);
            System.out.println("server wanted to say this: ");
            //edu.ucsb.cs56.W12.jcolicchio.issue535.JavaServer.clients.get(id).sendMessage("PMSG[0]Echo You said "+msg);
        }
    }
}