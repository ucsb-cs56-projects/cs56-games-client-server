package edu.ucsb.cs56.W12.jcolicchio.issue535;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * GomokuPanel is a gamepanel which allows the user to play a game of gomoku, sending moves and displaying the current turn, and
 * if applicable, the winner
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */
public class GomokuPanel extends GamePanel {
    GomokuGame game;
    JPanel menuButtons;

    JButton playSpecButton;
    LeaveButton leaveButton;
    NewGameButton newGameButton;
    SizesBox sizesBox;

    public boolean isPlaying;

    int offsetX, offsetY;
    int margin;
    double gridSize;
    int panelSize;
    int topMargin = 40;
    int bottomMargin = 30;
    
    int cells = 9;

    public GomokuPanel() {
        setLayout(new BorderLayout());
        menuButtons = new JPanel();
//        menuButtons.setLayout(new BoxLayout(menuButtons, BoxLayout.X_AXIS));
        add(BorderLayout.SOUTH, menuButtons);
        playSpecButton = new PlaySpecButton();
        playSpecButton.setAlignmentX(CENTER_ALIGNMENT);
        leaveButton = new LeaveButton();
        leaveButton.setAlignmentX(CENTER_ALIGNMENT);
        newGameButton = new NewGameButton();
        newGameButton.setAlignmentX(CENTER_ALIGNMENT);
        newGameButton.setEnabled(false);
        menuButtons.add(playSpecButton);
        menuButtons.add(leaveButton);
        menuButtons.add(newGameButton);

        sizesBox = new SizesBox();
        menuButtons.add(sizesBox);

        game = new GomokuGame();

        GomokuCanvas canvas = new GomokuCanvas();
        canvas.addMouseListener(canvas);
        add(BorderLayout.CENTER, canvas);

        isPlaying = false;
    }

    //TODO: be able to change grid size if 2 you are one of the two players BUT the game hasn't started yet
        //sends a request to change size which is shot down by the server if the other player placed the first piece
    //TODO: some kind of way to be notified when the game starts
    //TODO: the init/size/state system is terrible, fix it

    /**
     * handle data from server
     * @param string data
     */
    @Override
    public void handleMessage(String string) {
        System.out.println("handling as Gomoku: "+string);
        if(string.indexOf("INIT;") == 0) {
            game.init(9);
        } else if(string.indexOf("STATE[") == 0) {
            game.setState(string);
        } else if(string.indexOf("MOVE[") == 0) {
            String[] data = string.substring(5).split("]");
            int pid = Integer.parseInt(data[0]);
            String[] coords = data[1].split(",");
            int X = Integer.parseInt(coords[0]);
            int Y = Integer.parseInt(coords[1]);

            game.grid[Y][X] = pid;
            game.turn = 3-pid;
        } else if(string.indexOf("PLAYERS;") == 0) {
            String[] data = string.substring(8).split(",");
            int pid1 = Integer.parseInt(data[0]);
            int pid2 = Integer.parseInt(data[1]);
            System.out.println(pid1+", "+JavaClient.javaClient.clients.size());
            if(pid1 >= 0 && pid1 < JavaClient.javaClient.clients.size()) {
                game.player1 = JavaClient.javaClient.clients.get(Integer.parseInt(data[0]));
            } else
                game.player1 = null;
            if(pid2 >= 0 && pid2 < JavaClient.javaClient.clients.size())
                game.player2 = JavaClient.javaClient.clients.get(Integer.parseInt(data[1]));
            else
                game.player2 = null;

            //if the user is currently playing
            if(pid1 == JavaClient.javaClient.id || pid2 == JavaClient.javaClient.id) {
                isPlaying = true;
                //if the game has two players, and is ready to go
                if(game.player1 != null && game.player2 != null) {
                    newGameButton.setEnabled(true);
                    sizesBox.setEnabled(false);
                } else {
                    newGameButton.setEnabled(false);
                    sizesBox.setEnabled(true);
                }
            } else {
                isPlaying = false;
                newGameButton.setEnabled(false);
                if(game.player1 != null && game.player2 != null)
                    sizesBox.setEnabled(false);
                else
                    sizesBox.setEnabled(true);
            }

            if(isPlaying || game.player1 == null || game.player2 == null)
                playSpecButton.setEnabled(true);
            else
                playSpecButton.setEnabled(false);
        } else if(string.indexOf("WINNER;")==0) {
            game.winner = Integer.parseInt(string.substring(7));
        } else if(string.indexOf("SIZE;") == 0) {
            int size = Integer.parseInt(string.substring(5));
            game.init(size);
            cells = size;
            sizesBox.setSize(size);
        }
    }

    /**
     * canvas to draw game and allow interaction
     */
    class GomokuCanvas extends JPanel implements MouseListener {

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            int totalWidth = getWidth();
            int totalHeight = getHeight();

            int gridHeight = totalHeight - topMargin - bottomMargin;
            int gridWidth = totalWidth;
            if(gridHeight < gridWidth) {
                panelSize = gridHeight;
                offsetX = (totalWidth-panelSize)/2;
                offsetY = topMargin;
            } else {
                panelSize = gridWidth;
                offsetX = 0;
                offsetY = (gridHeight-panelSize)/2+topMargin;
            }

            gridSize = (double)(panelSize)/cells;
            margin = (int)(panelSize/(33*cells))+1;

            g.setColor(Color.white);
            g.fillRect(0,0,getWidth(),getHeight());
            if(!JavaClient.javaClient.connected || JavaClient.javaClient.clients == null)
                return;

            g.setColor(Color.orange);
            g.fillRect(offsetX,offsetY,panelSize,panelSize);
            
            g.setColor(new Color(0x333333));
            for(int i=0;i<game.cells;i++) {
                g.fillRect((int)(offsetX+gridSize*(i+0.5)-margin),(int)(offsetY+gridSize/2-margin),(int)(margin*2),(int)(panelSize-gridSize+2*margin));
                g.fillRect((int)(offsetX+gridSize/2-margin),(int)(offsetY+gridSize*(i+0.5)-margin),(int)(panelSize-gridSize+2*margin),(int)(margin*2));
            }

            synchronized (game) {
                for(int i=0;i<cells;i++) {
                    for(int j=0;j<cells;j++) {
                        int pid = game.grid[i][j];
                        if(pid == 0)
                            continue;

                        Graphics2D g2d = (Graphics2D)g;
                        if(pid == 1) {
                            g.setColor(Color.DARK_GRAY);
                        } else {
                            g.setColor(Color.LIGHT_GRAY);
                        }
                        g.fillOval((int)(offsetX + j * gridSize + margin), (int)(offsetY + i * gridSize + margin),
                                (int)(gridSize - 2*margin), (int)(gridSize - 2*margin));

                    }
                }

                String readyState = "";
                if(game.player1 != null) {
                    g.setColor(Color.red);
                    g.drawString("Player 1: "+game.player1.name,offsetX,15);
                    g.setColor(Color.DARK_GRAY);
                    g.fillOval(offsetX-25,offsetY-40,20,20);
                } else
                    readyState = "waiting for players";
                if(game.player2 != null) {
                    g.setColor(Color.blue);
                    g.drawString("Player 2: "+game.player2.name,offsetX,35);
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillOval(offsetX-25,offsetY-20,20,20);
                } else
                    readyState = "waiting for players";

                if(readyState.equals("")) {
                    if(game.winner == 1)
                        readyState = game.player1.name+" wins!";
                    else if(game.winner == 2)
                        readyState = game.player2.name+" wins!";
                    else if(game.turn == 1)
                        readyState = game.player1.name+"'s turn";
                    else
                        readyState = game.player2.name+"'s turn";
                }

                g.setColor(new Color(0x222222));
                g.drawString(readyState,offsetX+panelSize/2-45,offsetY+panelSize+20);
            }
        }
        @Override
        public void mouseClicked(MouseEvent mouseEvent){ }
        @Override
        public void mousePressed(MouseEvent mouseEvent){
            if(!isPlaying)
                return;

            //To change body of implemented methods use File | Settings | File Templates.
            int mX = mouseEvent.getX();
            int mY = mouseEvent.getY();
            int dX = mX-offsetX;
            int dY = mY-offsetY;
            int cellX = (dX*cells/panelSize);
            int cellY = (dY*cells/panelSize);
            System.out.println(mX+", "+mY+", "+dX+", "+dY+", "+cellX+", "+cellY);
            if(cellX >= 0 && cellX < cells && cellY >= 0 && cellY < cells) {
                JavaClient.javaClient.sendMessage("MOVE;" + cellX + "," + cellY);
            }
        }
        @Override
        public void mouseReleased(MouseEvent mouseEvent){ }
        @Override
        public void mouseEntered(MouseEvent mouseEvent){ }
        @Override
        public void mouseExited(MouseEvent mouseEvent){ }
    }

    /**
     * let player play or spectate
     */
    class PlaySpecButton extends JButton implements ActionListener{
        public boolean playing;
        public PlaySpecButton() {
            super("Play");
            playing = false;
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent){
            if(playing) {
                JavaClient.javaClient.sendMessage("MSG;/spec");
                setText("Play");
                playing = false;
            } else {
                JavaClient.javaClient.sendMessage("MSG;/play");
                setText("Spectate");
                playing = true;
            }
        }
    }

    /**
     * let player leave game for lobby
     */
    class LeaveButton extends JButton implements ActionListener{
        public LeaveButton() {
            super("Leave");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent){
            JavaClient.javaClient.sendMessage("MSG;/join lobby");
        }
    }

    /**
     * let player in game start new game
     */
    class NewGameButton extends JButton implements ActionListener {
        public NewGameButton() {
            super("New Game");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JavaClient.javaClient.sendMessage("MSG;/newgame");
        }
    }

    /**
     * let client change size of board
     */
    class SizesBox extends JComboBox implements ActionListener {
        private boolean hideAction;

        public SizesBox() {
            super(new String[]{"9x9", "13x13", "17x17"});
            hideAction = false;
            addActionListener(this);
        }
        
        public void setSize(int n) {
            int index = (n-9)/4;
            synchronized (this) {
                hideAction = true;
                setSelectedIndex(index);
                hideAction = false;
            }
        }

        public void actionPerformed(ActionEvent e) {
            if(hideAction)
                return;
            String text = (String)getSelectedItem();
            int size = 9;
            if(text.equals("9x9"))
                size = 9;
            else if(text.equals("13x13"))
                size = 13;
            else if(text.equals("17x17"))
                size = 17;
            JavaClient.javaClient.sendMessage("SIZE;"+size);
        }
    }
}
