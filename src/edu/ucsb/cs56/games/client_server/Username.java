package edu.ucsb.cs56.games.client_server;

/**
 * Username class stores information about a user, such as name, location, etc
 * rendered by the user list in javaclient
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class Username{
    String name;
    String location;
    public int style;
    
    public Username(String NAME, String LOCATION, int STYLE) {
        name = NAME;
        location = LOCATION;
        style = STYLE;
    }
    @Override
    public String toString() {
        if(location == null)
            return name;
        return name+location;
    }
}
