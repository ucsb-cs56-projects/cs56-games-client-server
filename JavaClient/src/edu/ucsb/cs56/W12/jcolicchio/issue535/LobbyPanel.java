package edu.ucsb.cs56.W12.jcolicchio.issue535;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 * The lobby panel is a panel which displays currently available games which, when clicked on, will take the user to a
 * new or in-progress copy of that game. Soon, buttons will take players to instruction screens where they can choose
 * a few settings and specify if they want any server, or specifically an empty one to start a new game
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class LobbyPanel extends GamePanel {
    public LobbyPanel() {
        setLayout(new FlowLayout());
        JoinGameButton ticTacToeButton = new JoinGameButton("TicTacToe");
        add(BorderLayout.NORTH, ticTacToeButton);
        JoinGameButton gomokuButton = new JoinGameButton("Gomoku");
        add(BorderLayout.NORTH, gomokuButton);
        JoinGameButton chessButton = new JoinGameButton("Chess");
        add(BorderLayout.NORTH, chessButton);
    }

    public void paintComponent(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent){
        System.out.println("HI");
//            sendMessage("MSG;/join TicTacToe");
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent){
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent){
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent){
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent){
        //To change body of implemented methods use File | Settings | File Templates.
    }

    class JoinGameButton extends JButton implements ActionListener{
        String name;
        public JoinGameButton(String text) {
            super(text);
            name = text;
            this.addActionListener(this);
        }
        @Override
        public void actionPerformed(ActionEvent actionEvent){
            JavaClient.javaClient.sendMessage("MSG;/join " + name);
        }
    }
}