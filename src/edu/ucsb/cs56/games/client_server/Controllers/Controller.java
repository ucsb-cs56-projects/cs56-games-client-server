package edu.ucsb.cs56.games.client_server.Controllers;

import java.util.ArrayList;

import edu.ucsb.cs56.games.client_server.Controllers.Network.ClientNetworkController;

/**
 * Service is an abstract class to be extended by all services
 * a service is the server-side connection between a client and the server's copy of a game,
 * passing data from the client to the game, as delegated by the clientconnect object on the server
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public abstract class Controller{
    private static ArrayList<String> serviceList;
    private static int numServices;
    public ArrayList<ClientNetworkController> clients;
    public String name;
    public int id;
    public int type;
    private static boolean _init;

    public static void initData() {
        _init = true;

        serviceList = new ArrayList<String>();
        serviceList.add("Lobby");
        serviceList.add("TicTacToe");
        serviceList.add("Gomoku");
        serviceList.add("Chess");
        numServices = serviceList.size();
    }

    /**
     * get the number of unique services
     * @return number of unique services
     */
    public static int getNumServices() {
        if(!_init)
            initData();
        return numServices;
    }

    /**
     * get the type of service by its listed type number
     * @param n the type number of a service
     * @return name of a kind of service
     */
    public static String getGameType(int n) {
        if(!_init)
            initData();
        if(n < 0 || n >= serviceList.size())
            return null;

        return serviceList.get(n);
    }

    /**
     * add a client to this service
     * @param client client to add
     */
    public abstract void addClient(ClientNetworkController client);

    /**
     * remove a client from this service
     * @param client client to remove
     */
    public abstract void removeClient(ClientNetworkController client);

    /**
     * send data to all clients on service
     * @param data data to send
     */
    public abstract void broadcastData(String data);

    /**
     * handle incoming data from a client
     * @param client client sending the data
     * @param data data to handle
     */
    public abstract void handleData(ClientNetworkController client, String data);

    /**
     * switch a client from one service to another
     * @param client client to switch
     * @param service service to switch to
     */
    public abstract void switchServices(ClientNetworkController client, Controller service);
}
