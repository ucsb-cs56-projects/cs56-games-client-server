package edu.ucsb.cs56.W12.jcolicchio.issue535;

import java.util.ArrayList;

/**
 * Client object is a standard way to store info about a client, whether on the server or client side
 * On the client site, client objects represent peer clients, since no actual variables for these clients are provided
 * and the knowledge of their existence is provided only via strings from the server
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class ClientObject{
    int id;
    String name;
    int location;

    boolean isOp;

    static ArrayList<Integer> colors;
    static boolean _init;

    public ClientObject(int n) {
        //new client object made, stores data about client
        id = n;
        name = "User"+n;
        location = 0;
    }
    
    public ClientObject(int n, String NAME, int LOCATION) {
        id = n;
        name = NAME;
        location = LOCATION;
    }

    public static void init() {
        colors = new ArrayList<Integer>();
        colors.add(0xff0000);
        colors.add(0xffff00);
        colors.add(0x00ff00);
        colors.add(0x00ffff);
        colors.add(0x0000ff);
        colors.add(0xff00ff);
    }
    
    public int getColor() {
        if(!_init) {
            init();
        }

        return colors.get(id%colors.size());
    }
}
