package edu.ucsb.cs56.games.client_server.Views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Game panel is a largely abstract panel that provides an outline for how panels should be formatted, as well as provides
 * basic functions every panel should have. Might consider turning this into an interface, but before the offlinepanel existed,
 * gamepanel was used when a client was disconnected
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class GameViewPanel extends JPanel implements MouseListener{
    public GameViewPanel() {
        setLayout(new BorderLayout());
    }

    /**
     * update the game
     */
    public void update() {

    }

    /**
     * handle incoming data from server
     * @param string data
     */
    public void handleMessage(String string) {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent){
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent){
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent){
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent){
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent){
    }
}
