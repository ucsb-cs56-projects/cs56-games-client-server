package edu.ucsb.cs56.games.client_server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 * offline panel provides two textfields for IP address and port, and a connect button
 * if the ip and port combination are invalid, it'll stall for a few seconds while it times out
 * in the future, i'd like to have a thread do this, so the gui doesn't freeze with the connect button clicked
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

//TODO: make new thread when connect is clicked, which is resolved as soon as the connection is made, but does not freeze the gui while waiting
public class OfflinePanel extends GamePanel {
    JTextField ip_box;
    JTextField port_box;
    ConnectButton connectButton;
    
    public OfflinePanel(String IP, int PORT) {
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        ip_box = new JTextField("127.0.0.1");
        ip_box.setPreferredSize(new Dimension(100,22));
        ip_box.setAlignmentY(JTextField.CENTER_ALIGNMENT);
        ip_box.setText(IP);
        port_box = new JTextField("12345");
        port_box.setPreferredSize(new Dimension(45,22));
        port_box.setAlignmentY(JTextField.CENTER_ALIGNMENT);
        port_box.setText(PORT+"");
        connectButton = new ConnectButton();
        connectButton.setAlignmentY(JButton.CENTER_ALIGNMENT);
        JPanel connect_panel = new JPanel();
        connect_panel.add(ip_box);
        connect_panel.add(port_box);
        connect_panel.add(connectButton);
        add(Box.createVerticalGlue());
        add(connect_panel);
        ip_box.requestFocus();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent){}

    @Override
    public void mousePressed(MouseEvent mouseEvent){}

    @Override
    public void mouseReleased(MouseEvent mouseEvent){}

    @Override
    public void mouseEntered(MouseEvent mouseEvent){}

    @Override
    public void mouseExited(MouseEvent mouseEvent){}

    public class ConnectButton extends JButton implements ActionListener{
        public ConnectButton() {
            super("Connect");
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent ev) {
            JavaClient.javaClient.connect(ip_box.getText(),Integer.parseInt(port_box.getText()));
        }
    }
}