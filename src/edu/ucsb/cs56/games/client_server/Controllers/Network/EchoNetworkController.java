package edu.ucsb.cs56.games.client_server.Controllers.Network;

import edu.ucsb.cs56.games.client_server.JavaServer;
import edu.ucsb.cs56.games.client_server.Models.ClientModel;

/**
 * Echo connect was removed from the server, but used to be a dummy client which, when privately messaged by a real client,
 * would respond via private message to that user with the same message
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class EchoNetworkController extends ClientNetworkController {
    public EchoNetworkController(int id) {
        super(null);
        client = new ClientModel(id,"Echo",0);
    }

    @Override
    public void sendMessage(String string) {
        //System.out.println("echo got msg: "+string);
        if(string.indexOf("PMSG[") == 0) {
            String[] data = string.substring(5).split("]");
            int id = Integer.parseInt(data[0]);
            if(id == client.getId())
                return;
            String msg = string.substring(5+data[0].length()+1);
            System.out.println("echo said this: "+msg);
            handleMessage("MSG;/msg "+JavaServer.clients.get(id).client.getName()+" "+string.substring(5+data[0].length()+1));
        }
    }
}