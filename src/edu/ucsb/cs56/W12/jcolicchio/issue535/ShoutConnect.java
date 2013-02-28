package edu.ucsb.cs56.W12.jcolicchio.issue535;

/**
 * Shout connect is a dummy client that listens for private messages and repeats them publically
 * commands like /nick, /join, etc are displayed as raw text to prevent abuse or griefing by users
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class ShoutConnect extends ClientConnect {
    public ShoutConnect(int id) {
        super(null);
        client = new ClientObject(id,"SHOUT",0);
    }

    @Override
    public void sendMessage(String string) {
        //System.out.println("shout got msg: "+string);
        if(string.indexOf("PMSG[") == 0) {
            String[] data = string.substring(5).split("]");
            int id = Integer.parseInt(data[0]);
            if(id == client.id)
                return;
            String msg = string.substring(5+data[0].length()+1);
            //handleMessage("MSG;"+string.substring(5+data[0].length()+1));
            System.out.println("shout said this: " + string.substring(5 + data[0].length() + 1));
//            if(string.substring(5+data[0].length()+1).indexOf("/me ") == 0)
            JavaServer.broadcastMessage("MSG["+client.id+"]"+string.substring(5+data[0].length()+1));
//            else
//                edu.ucsb.cs56.W12.jcolicchio.issue535.JavaServer.broadcastMessage("MSG[2]"+string.substring(5+data[0].length()+1));
        }
    }
}