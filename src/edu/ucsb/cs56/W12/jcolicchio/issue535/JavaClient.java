package edu.ucsb.cs56.W12.jcolicchio.issue535;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 * JavaClient is the main runnable client-side application, it allows users to connect to a server on a specific port
 * and chat with other connected users, as well as play games like tic tac toe, gomoku, and chess with them
 * it is composed of a user list, a message box, input box and send button for chatting, and a panel area to display
 * the lobby or current game
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

//start a java message client that tries to connect to a server at localhost:X
public class JavaClient implements KeyListener {
    public static JavaClient javaClient;

    Socket sock;
    InputStreamReader stream;
    BufferedReader reader;
    PrintWriter writer;

    ArrayList<ClientObject> clients;
    ArrayList<Integer> services;
    
    ArrayList<Message> messages;

    JFrame frame;
    Container container;
    GamePanel canvas;//the actual canvas currently being used by the gui
    GamePanel canvasRef;//a reference to the current canvas being used by the game logic
    JTextField inputBox;
    JButton sendButton;
    JEditorPane outputBox;

    JList userList;
    DefaultListModel listModel;

    int id;
    String name;
    int location;

    boolean[] Keys;
    
    InputReader thread;
    RefreshThread refreshThread;
    boolean connected;

    public static void main(String [] args) {
        javaClient = new JavaClient();
    }

    public JavaClient() {
        Res.init(this.getClass());
        frame = new JFrame("Java Games Online");
        frame.setSize(640, 512);
        frame.setMinimumSize(new Dimension(480,512));
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
                if(thread != null)
                    thread.running = false;
                if(connected)
                    sendMessage("DCON;Window Closed");

                System.exit(0);
            }
        });

        
        container = frame.getContentPane();
        canvas = new OfflinePanel(JavaServer.IP_ADDR,JavaServer.PORT);
        canvasRef = canvas;
        container.add(BorderLayout.CENTER,canvas);

        JPanel southPanel = new JPanel(new BorderLayout());
        container.add(BorderLayout.SOUTH, southPanel);

        SendListener listener = new SendListener();
        inputBox = new JTextField();
        inputBox.addActionListener(listener);
        sendButton = new JButton("Send");
        sendButton.addActionListener(listener);
        southPanel.setFocusable(true);
        canvas.setFocusable(true);

        canvas.addKeyListener(this);
        canvas.addMouseListener(canvas);
        inputBox.addKeyListener(this);

        southPanel.add(BorderLayout.EAST, sendButton);
        southPanel.add(BorderLayout.CENTER, inputBox);

        listModel = new DefaultListModel();


        userList = new JList(listModel);
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = userList.locationToIndex(e.getPoint());
                    //follow player into game
                    Username user = (Username)(listModel.getElementAt(index));
                    if(user != null)
                        sendMessage("MSG;/follow "+user.name);
                }
            }
        };
        userList.addMouseListener(mouseListener);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setLayoutOrientation(JList.VERTICAL);
        userList.setVisibleRowCount(-1);
        userList.setCellRenderer(new MyCellRenderer());

        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());
        container.add(BorderLayout.WEST, userPanel);
        userPanel.add(BorderLayout.CENTER,userScroll);
        FollowButton followButton = new FollowButton();
        MessageButton messageButton = new MessageButton();
        
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel,BoxLayout.X_AXIS));
        menuPanel.add(followButton);
        menuPanel.add(Box.createHorizontalGlue());
        menuPanel.add(messageButton);
        userPanel.add(BorderLayout.SOUTH,menuPanel);
        userScroll.setPreferredSize(new Dimension(160,100));

        outputBox = new JEditorPane("text/html", "");
        JScrollPane outputScroll = new JScrollPane(outputBox);
        outputBox.setEditable(false);
        southPanel.add(BorderLayout.NORTH, outputScroll);
        outputScroll.setPreferredSize(new Dimension(100, 100));

        frame.setVisible(true);

        Keys = new boolean[255];
        for(int i=0;i<255;i++)
            Keys[i] = false;

        //TODO: use the standardized list!!

        location = -1;
    }

    /** followbutton allows users to follow their friends into the game they're playing
     * this can also be achieved by double-clicking on a name in the user list
     */
    class FollowButton extends JButton implements  ActionListener {
        public FollowButton() {
            super("Follow");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Username user = (Username)userList.getSelectedValue();
            if(user != null)
               sendMessage("MSG;/follow "+user.name);
        }
    }

    /** messagebutton fills the input box with a command to send the specified user a message
     *
     */
    class MessageButton extends JButton implements  ActionListener {
        public MessageButton() {
            super("Message");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Username user = (Username)userList.getSelectedValue();
            if(user == null)
                return;

            inputBox.setText("/msg " + user.name + " ");
            //give inputbox focus
            inputBox.requestFocus();
        }
    }


    public void init() {
        clients = new ArrayList<ClientObject>();
        services = new ArrayList<Integer>();
        messages = new ArrayList<Message>();
    }

    /** updateClients updates the client list with the names and locations of everyone on the server
     * should be called whenever a user joins, leaves, or changes locations
     */
    public void updateClients() {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    synchronized (clients) {
                        listModel.clear();
                        if(location < 0)
                            return;
                        listModel.addElement(new Username(name,null,2));
                        for(int i=clients.size()-1;i>=0;i--) {
                            ClientObject client = clients.get(i);
                            if(client != null) {
                                if(client.id == id)
                                    continue;
                                if(client.location == location || services.size() <= client.location)
                                    listModel.insertElementAt((new Username(client.name,null,0)),1);
                                else {
//                                    System.out.println(client.location+", "+serviceList.size()+", "+services.size());
                                    listModel.addElement(new Username(client.name," ("+client.location+":"+Service.getGameType(services.get(client.location))+")",1));
                                }
                            }
                        }
                    }
                }
            }
        );
    }

    /** updateMessages updates the message box, and then scrolls down to the bottom to see the most recent
     * message. should be called whenever a new message is received
     */
    public void updateMessages() {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    String content = "";
                    for(int i=0;i<messages.size();i++) {
                        content += messages.get(i).toString() + "<br>";
                    }
                    outputBox.setText(content);
                    int caret = outputBox.getDocument().getLength()-1;
                    if(caret > 0)
                        outputBox.setCaretPosition(caret);
                }
            }
        );
    }

    /** connect is called when the player enters an IP and port number, and clicks connect
     * it attempts to connect the player to the associated running server if it exists
     * @param ip - the ip address string to connect to
     * @param port - the port number
     */
    public void connect(String ip, int port) {
        if(connected)
            return;
        try {
            System.out.println("Connecting to "+ip+":"+port);
            sock = new Socket(ip,port);
            System.out.println("Connected");
            connected = true;
            init();
            stream = new InputStreamReader(sock.getInputStream());
            reader = new BufferedReader(stream);
            writer = new PrintWriter(sock.getOutputStream());
            sendMessage("ACKNOWLEDGE ME!");
            thread = new InputReader();
            thread.start();
            refreshThread = new RefreshThread(this);
            refreshThread.start();
        } catch(IOException ex) {
            ex.printStackTrace();
            System.out.println("unable to connect");
            //System.out.println("quitting...");
            //System.exit(1);
        }
    }

    //public void update() {
    //    for(int i=0;i<clients.size();i++)
    //        if(clients.get(i) != null)
    //            clients.get(i).update();
    //}

    /** handleMessage is passed a string which has been sent from the server
     * it attempts to resolve the request but may forward it to the active game panel, if applicable
     * it manages things like users connecting, disconnecting, receiving private messages, nick changes, etc
     * whereas the game panel handles data regarding the current game
     * @param string the data from the server to handle
     */
    public void handleMessage(String string) {
        if(string.indexOf("CON;") == 0) {
            int pid = Integer.parseInt(string.substring(4));
            System.out.println("Client "+pid+" has connected");
            while(clients.size() <= pid)
                clients.add(null);
            if(clients.get(pid) == null)
                clients.set(pid, new ClientObject(pid));
            else
                sendMessage("INFO;");
            messages.add(new Message(clients.get(pid).name+" connected", "Server",true,false));
            updateClients();
            updateMessages();
        } else if(string.indexOf("DCON[") == 0) {
            String[] data = string.substring(5).split("]");
            int pid = Integer.parseInt(data[0]);
            System.out.println("Client " + pid + " has disconnected: " + data[1]);
            if(clients.size() > pid && clients.get(pid) != null) {
                messages.add(new Message(clients.get(pid).name + " disconnected: "+data[1], "Server", true, false));
                clients.set(pid, null);
            }
            updateClients();
            updateMessages();
            if(pid == id)
                thread.running = false;
        } else if(string.indexOf("MSG[") == 0) {
            String[] data = string.substring(4).split("]");
            int pid = Integer.parseInt(data[0]);
            if(clients.size() <= pid || clients.get(pid) == null)
                return;
            String msg = string.substring(4+data[0].length()+1);
            System.out.println("Client "+pid+" said "+msg);
            if(clients.size() > pid) {
                messages.add(new Message(msg,clients.get(pid).name,false,false));
                updateMessages();
            }
        } else if(string.indexOf("PMSG[") == 0) {
            String[] data = string.substring(5).split("]");
            int pid = Integer.parseInt(data[0]);
            String msg = string.substring(5+data[0].length()+1);
            System.out.println("Client "+pid+" privately said "+msg);
            if(clients.size() > pid) {
                messages.add(new Message(msg,clients.get(pid).name, true, false));
                updateMessages();
            }
        } else if(string.indexOf("RMSG[") == 0) {
            String[] data = string.substring(5).split("]");
            int pid = Integer.parseInt(data[0]);
            String msg = string.substring(5+data[0].length()+1);
            if(clients.size() > pid) {
                messages.add(new Message(msg,clients.get(pid).name,true,true));
                updateMessages();
            }
        } else if(string.indexOf("SMSG;") == 0) {
            String msg = string.substring(5);
            if(msg != null && msg.length() > 0) {
                messages.add(new Message(msg,"Server",true,false));
                updateMessages();
            }
        } else if(string.indexOf("ID;") == 0) {
            id = Integer.parseInt(string.substring(3));
            if(name == null)
                name = "User"+id;

            sendMessage("CON;");
            sendMessage("NAME;"+name);
            sendMessage("INFO;");
            System.out.println(location);
        } else if(string.indexOf("ALL;") == 0) {
            String[] connected = string.substring(4).split(";");
            for(int i=0;i<connected.length;i++) {
                String[] info = connected[i].split(",");
                if(clients.size() <= i)
                    clients.add(null);
                if(connected[i].equals(","))
                    continue;
                if(info[0].equals("")) {
                    if(clients.get(i) != null)
                        clients.set(i, null);
                } else {
                    clients.set(i, new ClientObject(i, info[0], Integer.parseInt(info[1])));
                    if(id == i)
                        changeLocation(Integer.parseInt(info[1]));
                }
            }
            //the problem is here, we need to have something else removing the clients from the list and re-adding them
            //otherwise when the thing redraws, it'll freak out
            updateClients();
        } else if(string.indexOf("SERV;") == 0) {
            String[] serv = string.substring(5).split(",");
            for(int i=0;i<serv.length;i++) {
                if(services.size() <= i)
                    services.add(null);
                services.set(i, Integer.parseInt(serv[i]));
            }
            updateClients();
            changeLocation(location);
        } else if(string.indexOf("NEW;") == 0) {
            services.add(Integer.parseInt(string.substring(4)));
        } else if(string.indexOf("NAME[") == 0) {
            String[] data = string.substring(5).split("]");
            int pid = Integer.parseInt(data[0]);
            String pname = data[1];
            if(clients.size() <= pid)
                return;
            if(clients.get(pid) == null)
                clients.set(pid, new ClientObject(id, pname, 0));
            //messages.add(new edu.ucsb.cs56.W12.jcolicchio.issue535.Message(clients.get(pid).name+" changed his name to "+pname, "Server",true,false,clients.get(0).getColor()));
            clients.get(pid).name = pname;
            if(pid == id)
                name = pname;
            updateClients();
            updateMessages();
        } else if(string.indexOf("MOVED[") == 0) {
            String[] data = string.substring(6).split("]");
            int pid = Integer.parseInt(data[0]);
            clients.get(pid).location = Integer.parseInt(data[1]);
            if(pid == id) {
                changeLocation(clients.get(id).location);
            }
            updateClients();
            updateMessages();
        }
        canvasRef.handleMessage(string);
    }

    /** changes the location of the client, in order to generate a service panel associated with
     * that location to start interacting with the specified service
     * @param L the service id number
     */
    public void changeLocation(int L) {
        if(location == L)
            return;
        location = L;
        if(location == -1) {
            canvasRef = new OfflinePanel(JavaServer.IP_ADDR,JavaServer.PORT);
        } else {

            int serviceType = services.get(location);
            if(serviceType == 0)
                canvasRef = new LobbyPanel();
            else if(serviceType == 1)
                canvasRef = new TicTacToePanel();
            else if(serviceType == 2)
                canvasRef = new GomokuPanel();
            else if(serviceType == 3)
                canvasRef = new ChessPanel();
        }

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    messages = new ArrayList<Message>();
                    //updateMessages();
                    container.remove(canvas);
                    canvas = canvasRef;
                    container.add(BorderLayout.CENTER, canvas);
                    canvas.addMouseListener(canvas);
                    //frame.validate();
                    container.validate();
                }
            }
        );
    }

    /** sends a message to the server, which might be a request for information, game data,
     * or a literal message to be broadcast to all users in the message box
     * @param string a string of data to send to the server
     */
    public void sendMessage(String string) {
        writer.println(string);
        writer.flush();
    }

    @Override
    public void keyTyped(KeyEvent keyEvent){ }

    @Override
    public void keyPressed(KeyEvent keyEvent){
        Keys[keyEvent.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent keyEvent){
        Keys[keyEvent.getKeyCode()] = false;
    }

    /** listens for the send button's action and sends a message, if connected
     *
     */
    class SendListener implements ActionListener {
        public SendListener() {

        }

        public void actionPerformed(ActionEvent event) {
            String message = inputBox.getText();
            if(message.length() == 0)
                return;

            inputBox.setText("");
            if(connected) {
                sendMessage("MSG;"+message);
            }
        }
    }

    /** input reader waits for data from the server and forwards it to the client
     *
     */
    class InputReader extends Thread implements Runnable {
        public boolean running;
        public void run() {
            String line;
            running = true;
            try {
                while(running && (line = reader.readLine()) != null) {
                    System.out.println("incoming... "+line);
                    handleMessage(line);
                }
            } catch(SocketException ex) {
                ex.printStackTrace();
                System.out.println("lost connection to server...");
            } catch(Exception ex) {
                ex.printStackTrace();
                System.out.println("crashed for some other reason, disconnecting...");
                writer.println("DCON;"+id);
                writer.flush();
            }

            try{
                sock.close();
            }catch(IOException e){
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            connected = false;
            outputBox.setText("");
            updateClients();
            changeLocation(-1);
            System.out.println("quitting, cause thread ended");
            //System.exit(0);
        }
    }

}

/** renders usernames with bold or italics
 * useful when a user is in another location
 * or to highlight the client's username
 */
class MyCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        Username user = (Username)value;
        if (user.style == 2) {// <= put your logic here
            c.setFont(c.getFont().deriveFont(Font.BOLD));
        } else if(user.style == 1) {
            c.setFont(c.getFont().deriveFont(Font.ITALIC));
        } else {
            c.setFont(c.getFont().deriveFont(Font.PLAIN));
        }
        return c;
    }
}

/**
 * refresh thread constantly repaints the application
 */
class RefreshThread extends Thread implements Runnable {
    public boolean running;
    JavaClient javaClient;
    public RefreshThread(JavaClient client) {
        running = false;
        javaClient = client;
    }

    public void run() {
        running = true;
        while(running) {
            //javaClient.update();
            SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            javaClient.canvas.repaint();
                        }
                    }
            );
            try {
                Thread.sleep(50);
            } catch(InterruptedException e) {
                e.printStackTrace();
                System.out.println("refresh thread broke");
            }
        }
    }
}