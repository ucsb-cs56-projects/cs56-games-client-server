package edu.ucsb.cs56.games.client_server.Models;

/**
 * Username class stores information about a user, such as name, location, etc
 * rendered by the user list in javaclient
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class UsernameModel{
    private String name;
    String location;
    public int style;
    
    public UsernameModel(String NAME, String LOCATION, int STYLE) {
        setName(NAME);
        location = LOCATION;
        style = STYLE;
    }
    @Override
    public String toString() {
        if(location == null)
            return getName();
        return getName()+location;
    }
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
