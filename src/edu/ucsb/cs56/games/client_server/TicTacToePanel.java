package edu.ucsb.cs56.games.client_server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;


/**
 * tictactoepanel allows user to interface with server while playing tic tac toe game, accepts input and draws board and
 * current player's turn on the screen
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class TicTacToePanel extends GamePanel {
    TicTacToeGame game;
    JPanel menuButtons;

    JButton playSpecButton;
    LeaveButton leaveButton;
    NewGameButton newGameButton;

    public boolean isPlaying;
    
    int offsetX, offsetY;
    int margin = 3;
    int gridSize;
    int panelSize;
    int topMargin = 40;
    int bottomMargin = 30;

    public TicTacToePanel() {
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

        game = new TicTacToeGame();
        
        TicTacToeCanvas canvas = new TicTacToeCanvas();
        canvas.addMouseListener(canvas);
        add(BorderLayout.CENTER, canvas);
        
        isPlaying = false;
    }

    @Override
    public void handleMessage(String string) {
        System.out.println("handling as tictactoe: "+string);
        if(string.indexOf("INIT;") == 0) {
            game.init();
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
            System.out.println(pid1+", "+JavaClient.javaClient.getClients().size());
            if(pid1 >= 0 && pid1 < JavaClient.javaClient.getClients().size()) {
                game.player1 = JavaClient.javaClient.getClients().get(Integer.parseInt(data[0]));
            } else
                game.player1 = null;
            if(pid2 >= 0 && pid2 < JavaClient.javaClient.getClients().size())
                game.player2 = JavaClient.javaClient.getClients().get(Integer.parseInt(data[1]));
            else
                game.player2 = null;
            
            if(pid1 == JavaClient.javaClient.getId() || pid2 == JavaClient.javaClient.getId()) {
                isPlaying = true;
                if(game.player1 != null && game.player2 != null)
                    newGameButton.setEnabled(true);
                else
                    newGameButton.setEnabled(false);
            } else {
                isPlaying = false;
                newGameButton.setEnabled(false);
            }
            
            if(isPlaying || game.player1 == null || game.player2 == null)
                playSpecButton.setEnabled(true);
            else
                playSpecButton.setEnabled(false);
        } else if(string.indexOf("WINNER;")==0) {
            game.winner = Integer.parseInt(string.substring(7));
        }
    }
    
    class TicTacToeCanvas extends JPanel implements MouseListener {

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

            gridSize = panelSize/3;

            g.setColor(Color.white);
            g.fillRect(0,0,getWidth(),getHeight());
            if(!JavaClient.javaClient.isConnected() || JavaClient.javaClient.getClients() == null)
                return;

            g.setColor(new Color(0x333333));
            g.fillRect(offsetX+gridSize-margin,offsetY, margin*2, gridSize*3);
            g.fillRect(offsetX+gridSize*2-margin,offsetY, margin*2, gridSize*3);
            g.fillRect(offsetX,offsetY+gridSize-margin, gridSize*3, margin*2);
            g.fillRect(offsetX,offsetY+gridSize*2-margin, gridSize*3, margin*2);


            Graphics2D g2d = (Graphics2D)g;
            g2d.setStroke(new BasicStroke(5));
            
            for(int i=0;i<3;i++) {
                for(int j=0;j<3;j++) {
                    int pid = game.grid[i][j];
                    if(pid == 0)
                        continue;

                    if(pid == 1) {
                        g2d.setColor(Color.RED);
                        GeneralPath path = new GeneralPath();
                        path.moveTo(offsetX + j * gridSize + 3*margin, offsetY + i * gridSize + 3*margin);
                        path.lineTo(offsetX+(j+1)*gridSize - 3*margin, offsetY+(i+1)*gridSize - 3*margin);
                        path.moveTo(offsetX+(j+1)*gridSize - 3*margin, offsetY + i * gridSize + 3*margin);
                        path.lineTo(offsetX + j * gridSize + 3*margin, offsetY+(i+1)*gridSize - 3*margin);
                        g2d.draw(path);
                    } else {
                        g.setColor(Color.BLUE);
                        g.drawOval( offsetX + j * gridSize + 3*margin, offsetY + i * gridSize + 3*margin,
                                gridSize - 6*margin, gridSize - 6*margin);
                    }

                }
            }

            g2d.setStroke(new BasicStroke(2));
            String readyState = "";
            if(game.player1 != null) {
                g.setColor(Color.red);
                g.drawString("Player 1: " + game.player1.getName(), offsetX, 15);
                GeneralPath path = new GeneralPath();
                path.moveTo(offsetX-23,offsetY-38);
                path.lineTo(offsetX-7, offsetY-22);
                path.moveTo(offsetX-7,offsetY-38);
                path.lineTo(offsetX-23, offsetY-22);
                g2d.draw(path);
            } else
                readyState = "waiting for players";
            if(game.player2 != null) {
                g.setColor(Color.blue);
                g.drawString("Player 2: "+game.player2.getName(),offsetX,35);
                g.drawOval( offsetX -23, offsetY - 18,
                        16, 16);
            } else
                readyState = "waiting for players";

            if(readyState.equals("")) {
                if(game.winner == 1)
                    readyState = game.player1.getName()+" wins!";
                else if(game.winner == 2)
                    readyState = game.player2.getName()+" wins!";
                else if(game.turn == 1)
                    readyState = game.player1.getName()+"'s turn";
                else
                    readyState = game.player2.getName()+"'s turn";
            }

            g.setColor(new Color(0x222222));
            g.drawString(readyState,offsetX+panelSize/2-45,offsetY+panelSize+20);
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
            int cellX = (dX*3/panelSize);
            int cellY = (dY*3/panelSize);
            System.out.println(mX+", "+mY+", "+dX+", "+dY+", "+cellX+", "+cellY);
            if(cellX >= 0 && cellX < 3 && cellY >= 0 && cellY < 3) {
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
}
