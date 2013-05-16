package edu.ucsb.cs56.games.client_server.Controllers;
//ChatService is basically a chat channel, it maintains a list of clients connected specifically to itself, and can send
//data to all clients connected to it
// a /msg command is handled
//edu.ucsb.cs56.W12.jcolicchio.issue535.ChatService can do things such as message, private message, rebound message, and interpret OP commands such as kick,
//ban, mute, etc, and relay them back to the edu.ucsb.cs56.W12.jcolicchio.issue535.JavaServer for processing
    //for now, it'll also have to accept commands like /join, /play, /leave

import java.util.ArrayList;

import edu.ucsb.cs56.games.client_server.JavaServer;
import edu.ucsb.cs56.games.client_server.Controllers.Network.ClientNetworkController;

/**
* Chat service is a service that most services will extend, it provides functionality for handling messages and chat-related data
*
* @author Joseph Colicchio
* @version for CS56, Choice Points, Winter 2012
*/

public class ChatController extends Controller {
    public ChatController() {
        clients = new ArrayList<ClientNetworkController>();
    }

    /** adds a client to the chat
     * @param client a clientconnect object representing the new client
     */
    public void addClient(ClientNetworkController client) {
        if(!clients.contains(client))
            clients.add(client);
        System.out.println(clients+", "+client);
        broadcastData("SMSG;" + client.client.getName() + " joined");
    }

    /** removes a client from the chat
     * @param client a clientconnect to remove from this service
     */
    public void removeClient(ClientNetworkController client) {
        clients.remove(client);
        broadcastData("SMSG;" + client.client.getName() + " left");
    }

    /** broadcasts data to all clients connected to THIS SERVICE specifically
     * instead of all users connected to the entire server
     * @param data - data to send
     */
    public void broadcastData(String data) {
        System.out.println("br: "+data);
        for(int i=0;i<clients.size();i++)
            clients.get(i).sendMessage(data);
    }

    /**
     * handles incoming data from a client
     * @param client the client sending the data
     * @param command the data to handle
     */
    public void handleData(ClientNetworkController client, String command) {
        System.out.println("lobby handling message: "+command);
        if(command.indexOf("MSG;") == 0) {
            //if incoming starts with MSG;, check for commands
            String message = command.substring(4);
            if(message.indexOf("/nick ")==0) {
                //if the command is /nick, try to rename using rename function
                client.rename(message.substring(6));
            } else if(message.indexOf("/op ") == 0) {
                //if the command is /op, try to op using op function
                client.op(message.substring(4));
            } else if(message.indexOf("//bbq") == 0) {
                //if command is //bbq, go ahead and OP user
                //edu.ucsb.cs56.W12.jcolicchio.issue535.JavaServer.broadcastMessage("OP;"+client.name);
                client.client.setOp(true);
                JavaServer.broadcastMessage("SMSG;"+client.client.getName()+" is OP! Run for your lives!");
            } else if(message.indexOf("/kick ") == 0 || message.indexOf("/k ") == 0) {
                if(!client.client.isOp()) {
                    client.fromServer("You cannot kick someone unless you are an OP");
                    return;
                }
                String[] data = message.split(" ");
                if(data.length < 2 || data[1].length() == 0)
                    return;
                client.kick(message.substring(data[0].length() + 1));
            } else if(message.indexOf("/ban") == 0 || message.indexOf("/b") == 0) {
                if(!client.client.isOp()) {
                    client.fromServer("You cannot ban someone unless you are an OP");
                    return;
                }
                String[] data = message.split(" ");
                if(data.length < 2 || data[1].length() == 0)
                    return;
                client.ban(message.substring(data[0].length() + 1));
            } else if(message.indexOf("/kickban") == 0 || message.indexOf("/kb") == 0) {
                if(!client.client.isOp()) {
                    client.fromServer("You cannot kickban someone unless you are an OP");
                    return;
                }
                String[] data = message.split(" ");
                if(data.length < 2 || data[1].length() == 0)
                    return;
                client.kickBan(message.substring(data[0].length() + 1));
            } else if(message.indexOf("/unban ") == 0){
                if(!client.client.isOp()) {
                    client.fromServer("You cannot unban someone unless you are an OP");
                    return;
                }
                String[] data = message.split(" ");
                if(data.length < 2 || data[1].length() == 0)
                    return;
                client.unban(message.substring(data[0].length() + 1));
            } else if(message.indexOf("/msg ") == 0) {
                //if command is /msg, get the name and message, and private message the user the message
                String[] data = message.substring(5).split(" ");
                if(data.length < 2)
                    return;
                int id = JavaServer.findClientByName(data[0]);
                if(id >= 0) {
                    String msg = message.substring(5+data[0].length()+1);
                    //send message back to user
                    client.sendMessage("RMSG[" + id + "]" + message.substring(5 + data[0].length() + 1));

                    System.out.println("private message from "+JavaServer.clients.get(id).client.getName()+" to "+data[0]+": "+msg);
                    JavaServer.clients.get(id).sendMessage("PMSG["+client.client.getId()+"]"+msg);
                } else {
                    client.sendMessage("SMSG;" + data[0] + " not on server!");
                }

            } else if(message.indexOf("/quit") == 0) {
                if(message.length() > 5)
                    client.disconnect(message.substring(5));
                else
                    client.disconnect("Disconnect");
            } else if(message.indexOf("/new ") == 0) {
                String serviceName = message.substring(5);
                int pid = JavaServer.findServiceByName(serviceName,true);
                System.out.println("service "+pid+" was found!");
                if(pid < 0 || pid >= JavaServer.services.size())
                    return;
                Controller service = JavaServer.services.get(pid);
                switchServices(client, service);
            } else if(message.indexOf("/join ") == 0) {
                String serviceName = message.substring(6);
                int pid = JavaServer.findServiceByName(serviceName,false);
                System.out.println("service "+pid+" was found!");
                if(pid < 0 || pid >= JavaServer.services.size())
                    return;
                Controller service = JavaServer.services.get(pid);
                switchServices(client, service);
            } else if(message.indexOf("/follow ") == 0) {
                String name = message.substring(8);
                int id = JavaServer.findClientByName(name);
                if(id < 0)
                    return;
                Controller service = JavaServer.services.get(JavaServer.clients.get(id).client.getLocation());
                switchServices(client, service);
            } else
                broadcastData("MSG["+client.client.getId()+"]"+message);
        }
    }

    /**
     * switches the client from one service to another
     * @param client the client to switch
     * @param service the service to switch to
     */
    @Override
    public void switchServices(ClientNetworkController client, Controller service){
        if(client.client.getLocation() == service.id)
            return;
        client.currentService.removeClient(client);
        JavaServer.broadcastMessage("MOVED[" + client.client.getId() + "]" + service.id);
        service.addClient(client);
        client.currentService = service;
        client.client.setLocation(service.id);
    }
}