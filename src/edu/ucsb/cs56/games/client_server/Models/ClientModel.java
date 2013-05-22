package edu.ucsb.cs56.games.client_server.Models;

import java.util.ArrayList;

/**
 * Client object is a standard way to store info about a client, whether on the server or client side
 * On the client site, client objects represent peer clients, since no actual variables for these clients are provided
 * and the knowledge of their existence is provided only via strings from the server
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class ClientModel{
    private int id;
    private String name;
    private int location;

    private boolean isOp;

    static ArrayList<Integer> colors;
    static boolean _init;

    public ClientModel(int n) {
        //new client object made, stores data about client
        setId(n);
        setName("User"+n);
        setLocation(0);
    }
    
    public ClientModel(int n, String NAME, int LOCATION) {
        setId(n);
        setName(NAME);
        setLocation(LOCATION);
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

        return colors.get(getId()%colors.size());
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isOp() {
		return isOp;
	}

	public void setOp(boolean isOp) {
		this.isOp = isOp;
	}

	public int getLocation() {
		return location;
	}

	public void setLocation(int location) {
		this.location = location;
	}
}
