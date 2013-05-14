package edu.ucsb.cs56.games.client_server;

import javax.swing.*;
import javax.swing.text.Utilities;

import edu.ucsb.cs56.games.client_server.Controllers.ChessController;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * JavaServer is the main server-side application, can be run without gui by using a port number as a single argument
 * on the command line. keeps track of clients connected and broadcasts data to one or multiple clients by setting up
 * clientconnect objects for each of them and keeping a list of users connected.
 * clientconnect handles server-related input from users and, if necessary, can query the server to find users by name
 * or an available, open game service
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

//start a java message server that listens for connections to port X and then connects the client 
public class JavaServer{
    //this belongs to the server itself, independent of the chat standards
    public static ArrayList<ClientConnect> clients;
    public static ArrayList<Service> services;
    public static LobbyService lobby;
    
    public JTextField port_box;
    public ConnectButton connectButton;
    public JLabel status;
    public boolean running;

    //this could go in either, it's used mostly for the chat
    public static ArrayList<String> bannedList;

    //public static final String IP_ADDR = "184.189.233.37"; // home
    //public static final String IP_ADDR = "184.189.240.201"; // ro's house
    //public static final String IP_ADDR = "128.111.57.217";
    //public static final String IP_ADDR = "128.111.57.217";
    //public static final String IP_ADDR = "169.231.108.15"; // temporary wireless
    public static final String IP_ADDR = "127.0.0.1"; // localhost
    public static final int PORT = 12345;

    public static boolean connected;
    public static JavaServer javaServer;
    public MainThread mainThread;
    public String runningOn;
    public int portNum;
    public static boolean nogui;

    public static void main(String [] args) {
        if(args.length > 0) {
            try {
                int portNum = Integer.parseInt(args[0]);
                nogui = true;
                javaServer = new JavaServer();
                javaServer.connect(portNum);
            } catch(Exception ex) {
                System.out.println("bad port: "+args[0]);
                System.exit(1);
            }
        } else
            javaServer = new JavaServer();
    }

    /**
     * start server on specified port
     * @param port port number to bind to
     */
    public void connect(int port) {
        try {
            URL ipGetter = new URL(" http://api.externalip.net/ip/");
            BufferedReader ip = new BufferedReader(new InputStreamReader(ipGetter.openStream()));
            runningOn = ip.readLine();
            System.out.println(runningOn);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        mainThread = new MainThread(port);
        mainThread.start();
    }

    /**
     * gracefully stop server for whatever reason
     */
    public void stop() {
        running = false;
        try {
            Socket socket = new Socket("127.0.0.1",portNum);
        } catch(Exception ex) {
            ex.printStackTrace();
            if(!nogui)
                javaServer.status.setText("Couldn't stop");
        }
    }
    
    public JavaServer() {
        if(nogui)
            return;
        JFrame mainFrame = new JFrame();
        Container container = mainFrame.getContentPane();
        JPanel main = new JPanel();
        //main.setLayout(new BorderLayout());
        container.add(BorderLayout.CENTER, main);
        port_box = new JTextField();
        port_box.setText(PORT + "");
        main.add(port_box);
        connectButton = new ConnectButton();
        main.add(connectButton);
        status = new JLabel();
        main.add(status);
        //mainFrame.pack();
        mainFrame.setSize(200,100);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
    }

    /**
     * update gui with number of clients
     */
    public static void updateServerGUI() {
        if(nogui)
            return;
        if(javaServer.running)
            javaServer.status.setText(javaServer.runningOn+", "+clients.size()+" user"+(clients.size()!=1?"s":""));
        else
            javaServer.status.setText("Offline");
    }

    /**
     * broadcast data to all clients on server
     * @param string data to send
     */
    public static void broadcastMessage(String string) {
        System.out.println("broadcasting... "+string);
        synchronized (clients) {
            for(int i=0;i<clients.size();i++)
                if(clients.get(i) != null)
                    clients.get(i).sendMessage(string);
        }
    }

    /** find a client given a string name
     *
     * @param name name of client
     * @return id of client or -1 if not found
     */
    public static int findClientByName(String name) {
        synchronized (clients) {
            for(int i=0;i<clients.size();i++) {
                if(clients.get(i) != null && clients.get(i).client.getName().equalsIgnoreCase(name))
                    return i;
            }
        }

        return -1;
    }
    
    public static String findUnusedName() {
        String name = "";
        int id = 0;
        int foundAt;
        synchronized (clients) {
            do {
                name = "User"+id;
                foundAt = findClientByName(name);
                id++;
            } while(foundAt != -1);
        }
        return name;
    }

    /**
     * finds a service by name, or creates a new one if none found
     * @param name name of type of service
     * @param empty if service must be empty
     * @return the id of the server being searched for
     */
    public static int findServiceByName(String name, boolean empty) {
        for(int i=0;i<services.size();i++) {
            if(services.get(i) != null && services.get(i).name.equalsIgnoreCase(name))
                if(!empty || services.get(i).clients.size() <= 1)
                    return i;
        }
        
        int serviceType = -1;
        for(int i=0;i<Service.getNumServices();i++) {
            if(Service.getGameType(i).equalsIgnoreCase(name))
                serviceType = i;
        }
        
        if(serviceType == -1)
            return -1;

        int serviceID = services.size();

        Service service = null;
        if(serviceType == 0)
            service = new LobbyService(serviceID);
        else if(serviceType == 1)
            service = new TicTacToeService(serviceID);
        else if(serviceType == 2)
            service = new GomokuService(serviceID);
        else if(serviceType == 3)
            service = new ChessController(serviceID);

        if(service == null)
            return -1;

        services.add(service);
        broadcastMessage("NEW;"+serviceType);

//        service.addClient(clients.get(0));
        
        return serviceID;
    }

    //chat convention

    /** ban ip from server
     *
     * @param IP ip to ban
     */
    public static void banIP(String IP) {
        for(int i=0;i<bannedList.size();i++) {
            if(IP.equals(bannedList.get(i)))
                return;
        }
        System.out.println("B&: "+IP);
        bannedList.add(IP.split(":")[0]);
    }

    //chat convention

    /**
     * unban an IP
     * @param IP ip to unban
     */
    public static void unbanIP(String IP) {
        for(int i=0;i<bannedList.size();i++) {
            if(IP.equals(bannedList.get(i))) {
                bannedList.remove(i);
                return;
            }
        }
    }

    //chat convention

    /**
     * is IP banned?
     * @param IP ip in question
     * @return if IP is banned
     */
    public static boolean isBanned(String IP) {
        String ADDR = IP.split(":")[0];
        for(int i=0;i<bannedList.size();i++) {
            if(ADDR.equals(bannedList.get(i)))
                return true;
        }
        return false;
    }

    /**
     * a button to allow server to start or stop
     */
    class ConnectButton extends JButton implements ActionListener {
        public ConnectButton() {
            super("Start Server");
            addActionListener(this);
        }
        
        public void actionPerformed(ActionEvent ev) {
            if(connected) {
                setText("Start Server");
                javaServer.stop();
            } else {
                setText("Stop Server");
                javaServer.connect(Integer.parseInt(port_box.getText()));
            }
        }
    }

    /** thread to prevent gui from freezing on connect
     *
     */
    class MainThread extends Thread implements Runnable {
        public MainThread(int P) {
            portNum = P;
        }
        
        public void run() {
            running = true;
            clients = new ArrayList<ClientConnect>();
            bannedList = new ArrayList<String>();

            services = new ArrayList<Service>();
            lobby = new LobbyService(0);
            services.add(lobby);

            //clients.add(new edu.ucsb.cs56.W12.jcolicchio.issue535.EchoConnect(clients.size()));
            //clients.add(new edu.ucsb.cs56.W12.jcolicchio.issue535.ShoutConnect(clients.size()));
            ServerSocket serverSock = null;
            Socket sock = null;
            System.out.println("total users: "+clients.size());
            try {
                connected = true;
                serverSock = new ServerSocket(portNum);
                
                while(running) {
                    //a new client wants to connect
                    System.out.println("waiting for next connection...");
                    updateServerGUI();
                    sock = serverSock.accept();
                    if(!running) {
                        updateServerGUI();
                        sock.close();
                        break;
                    }

                    System.out.println("incoming connecting...");
                    //give them a client object, run it in a thread
                    ClientConnect conn = new ClientConnect(sock);
                    Thread thread = new Thread(conn);
                    thread.start();
                    System.out.println("thread started");
                }
            } catch(IOException ex) {
                if(!nogui) {
                    javaServer.status.setText("Port already taken");
                    javaServer.connectButton.setText("Start Server");
                }
                ex.printStackTrace();
                System.out.println("requested port already taken. quitting...");
            }
            try {
                for(int i=0;i<clients.size();i++)
                    if(clients.get(i) != null)
                        clients.get(i).disconnect("Server stopping");
                int left = 0;
                do {
                    left = 0;
                    for(int i=0;i<clients.size();i++) {
                        if(clients.get(i) != null)
                            left++;
                    }
                    Thread.sleep(50);
                } while(left > 0);
                serverSock.close();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            connected = false;
        }
    }
}


//server update thread goes here