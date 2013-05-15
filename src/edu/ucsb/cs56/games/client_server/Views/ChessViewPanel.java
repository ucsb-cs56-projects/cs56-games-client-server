package edu.ucsb.cs56.games.client_server.Views;

import javax.swing.*;

import edu.ucsb.cs56.games.client_server.JavaClient;
import edu.ucsb.cs56.games.client_server.Models.ChessModel;
import edu.ucsb.cs56.games.client_server.Models.ResModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

/**
 * Chess panel to be displayed in client, gets data from server and renders it, also provides interactivity between player and server
 * things such as highlighting valid moves on the board, displaying whose turn it is, whether a player is in check, if a player has won,
 * etc. Will soon have animations to illustrate moves. Currently highlights last moved piece
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class ChessViewPanel extends GameViewPanel {
    ChessModel game;
    int player1, player2;
//    ArrayList<Character> capt1, capt2;
    JPanel menuButtons;

    JButton playSpecButton;
    LeaveButton leaveButton;
    NewGameButton newGameButton;
    FlippedBox flippedBox;
    Image pieces;

    public boolean isPlaying;
    public int playerID;

    int offsetX, offsetY;
    int margin;
    double gridSize;
    int panelSize;
    int topMargin = 40;
    int bottomMargin = 30;

    int cells = 8;
    int selectX = -1;
    int selectY = -1;

    //the coordinates of the last move made by a player
    int lastMoveX = -1;
    int lastMoveY = -1;
    //a timer for moving the piece across the gameboard
    //when a new move happens, if the animation isn't done, finish it
    //don't remove any victim pieces until it reaches its destination
    //this doesn't work for en passant or castling
    int animTimer;
    int oldPosX = -1;
    int oldPosY = -1;
    boolean[][] validMoves;

    boolean flipped;
    //if a player is in check, check = that player's id
    int check;
    boolean promoting;

    //TODO: service panel should ask for information about service, i.e. state, players, etc

    public ChessViewPanel() {
        pieces = ResModel.ChessPieces;
        setLayout(new BorderLayout());
        menuButtons = new JPanel();
        add(BorderLayout.SOUTH, menuButtons);
        playSpecButton = new PlaySpecButton();
        playSpecButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        leaveButton = new LeaveButton();
        leaveButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        newGameButton = new NewGameButton();
        newGameButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        newGameButton.setEnabled(false);
        menuButtons.add(playSpecButton);
        menuButtons.add(leaveButton);
        menuButtons.add(newGameButton);

        flippedBox = new FlippedBox();
        menuButtons.add(flippedBox);
        menuButtons.add(new JLabel("Flip Board?"));

        game = new ChessModel();

        ChessCanvas canvas = new ChessCanvas();
        canvas.addMouseListener(canvas);
        add(BorderLayout.CENTER, canvas);

        flipped = false;
        check = 0;
//        capt1 = new ArrayList<Character>();
//        capt2 = new ArrayList<Character>();
        isPlaying = false;
        player1 = player2 = -1;
        validMoves = new boolean[8][8];
        for(int i=0;i<8;i++)
            for(int j=0;j<8;j++)
                validMoves[i][j] = false;
        JavaClient.javaClient.sendMessage("STATE;");
    }

    //TODO: background notification sounds

    /**
     * handles data from the server
     * @param string data to process
     */
    @Override
    public void handleMessage(String string) {
        System.out.println("handling as Chess: "+string);
        if(string.indexOf("INIT;") == 0) {
            game.init();
//            capt1 = new ArrayList<Character>();
//            capt2 = new ArrayList<Character>();
        } else if(string.indexOf("STATE[") == 0) {
            game.setState(string);
            check = (game.isInCheck(game.turn)?game.turn:0);
            selectX = selectY = -1;
        } else if(string.indexOf("MOVE[") == 0) {
            String[] data = string.substring(5).split("]");
            int pid = Integer.parseInt(data[0]);
            String[] coords = data[1].split(",");
            int X1 = Integer.parseInt(coords[0]);
            int Y1 = Integer.parseInt(coords[1]);
            int X2 = Integer.parseInt(coords[2]);
            int Y2 = Integer.parseInt(coords[3]);

//            if(Character.isLetter(game.grid[Y2][X2]))
//                if(game.turn == 1)
//                    capt1.add(game.grid[Y2][X2]);
//                else
//                    capt2.add(game.grid[Y2][X2]);

//            game.grid[Y2][X2] = game.grid[Y1][X1];
//            game.grid[Y1][X1] = '0';
            game.tryMove(X1,Y1,X2,Y2);
            game.turn = pid;
            check = (game.isInCheck(game.turn)?game.turn:0);
            selectX = selectY = -1;
            lastMoveX = X2;
            lastMoveY = Y2;
        } else if(string.indexOf("PLAYERS;") == 0) {
            String[] data = string.substring(8).split(",");
            player1 = Integer.parseInt(data[0]);
            player2 = Integer.parseInt(data[1]);
            System.out.println(player1+", "+JavaClient.javaClient.getClients().size());
            if(player1 >= 0 && player1 < JavaClient.javaClient.getClients().size()) {
                game.player1 = JavaClient.javaClient.getClients().get(player1);
            } else
                game.player1 = null;
            if(player2 >= 0 && player2 < JavaClient.javaClient.getClients().size())
                game.player2 = JavaClient.javaClient.getClients().get(player2);
            else
                game.player2 = null;

            //if the user is currently playing
            if(player1 == JavaClient.javaClient.getId() || player2 == JavaClient.javaClient.getId()) {
                isPlaying = true;
                if(player1 == JavaClient.javaClient.getId())
                    playerID = 1;
                else {
                    flippedBox.setSelected(true);
                    flipped = true;
                    playerID = 2;
                }
                //if the game has two players, and is ready to go
                if(game.player1 != null && game.player2 != null) {
                    newGameButton.setEnabled(true);
//                    sizesBox.setEnabled(false);
                } else {
                    newGameButton.setEnabled(false);
//                    sizesBox.setEnabled(true);
                }
            } else {
                isPlaying = false;
                playerID = 0;
                newGameButton.setEnabled(false);
//                if(game.player1 != null && game.player2 != null)
//                    sizesBox.setEnabled(false);
//                else
//                    sizesBox.setEnabled(true);
            }

            if(isPlaying || game.player1 == null || game.player2 == null)
                playSpecButton.setEnabled(true);
            else
                playSpecButton.setEnabled(false);
        } else if(string.indexOf("WINNER;")==0) {
            game.winner = Integer.parseInt(string.substring(7));
            System.out.println("winner: "+game.winner);
        } else if(string.indexOf("SIZE;") == 0) {
            int size = Integer.parseInt(string.substring(5));
            game.init();
//            sizesBox.setSize(size);
        } else if(string.indexOf("PROMOTE[") == 0) {
            String[] data = string.substring(8).split("]");
            int pid = Integer.parseInt(data[0]);
            String[] coords = data[1].split(",");
            int X1 = Integer.parseInt(coords[0]);
            int Y1 = Integer.parseInt(coords[1]);
            int X2 = Integer.parseInt(coords[2]);
            int Y2 = Integer.parseInt(coords[3]);
            
            game.grid[Y2][X2] = game.grid[Y1][X1];
            game.grid[Y1][X1] = '0';

            lastMoveX = X2;
            lastMoveY = Y2;
            
            if(playerID == pid)
                promoting = true;
        } else if(string.indexOf("PROMOTE;") == 0) {
            String[] data = string.substring(8).split(",");
            int X = Integer.parseInt(data[0]);
            int Y = Integer.parseInt(data[1]);
            char piece = data[2].charAt(0);
            game.grid[Y][X] = piece;
            check = (game.isInCheck(game.turn)?game.turn:0);
            game.turn = 3-game.turn;
        }
    }

    /**
     * a chess canvas to paint the game state and interact with the user
     */
    class ChessCanvas extends JPanel implements MouseListener {

        //TODO: animations for movement
        //TODO: along with animations, highlight last move
            //this'll also depend on info coming in, like en passant is weird, and castling, and pawn promotion, and how does a piece "die"?
        //TODO: pawn promotion should look nicer, right now it serves its purpose but doesn't look nice
        //TODO: attack color
        //TODO: general problem: don't repaint every frame, only when changes happen?
        //TODO: general problem: if two players, change settings to allow edits until the first move is played
            //this probably means a new standard for when the game starts
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            int totalWidth = getWidth();
            int totalHeight = getHeight();

            double gridHeight = totalHeight - topMargin - bottomMargin;
            double gridWidth = totalWidth;
            if(3*gridHeight < 2*gridWidth) {
                panelSize = (int)gridHeight;
                offsetX = (int)(totalWidth-panelSize*1.5)/2+panelSize*1/4;
                offsetY = topMargin;
            } else {
                panelSize = (int)gridWidth*2/3;
                offsetX = panelSize*1/4;
                offsetY = (int)((gridHeight-panelSize)/2+topMargin);
            }

            gridSize = (double)(panelSize)/cells;
            margin = (int)(panelSize/(33*cells))+1;

            g.setColor(Color.white);
            g.fillRect(0,0,getWidth(),getHeight());
            if(!JavaClient.javaClient.isConnected() || JavaClient.javaClient.getClients() == null)
                return;

            g.setColor(new Color(0x333333));

            synchronized (game) {
                for(int i=0;i<8;i++) {
                    for(int j=0;j<8;j++) {
                        int cellX = (flipped?7-j:j);
                        int cellY = (flipped?7-i:i);
                        g.setColor(new Color((cellX+cellY)%2==0?0xaaaaaa:0x777777));
                        if(lastMoveX == j && lastMoveY == i)
                            g.setColor(new Color((cellX+cellY)%2==0?0xcccccc:0x555555));
                        if(selectX != -1 && selectY != -1) {
                            if(selectX == j && selectY == i)
                                g.setColor(new Color(0x88ccff));
                            else if(validMoves[j][i])
                                if(Character.isLetter(game.grid[i][j]) || (Character.toLowerCase(game.grid[selectY][selectX]) == 'p' && selectX != j))
                                    g.setColor(new Color(0xeeaa66));
                                else
                                    g.setColor(new Color(0x66aaee));
                        }
                        g.fillRect((int)(offsetX+gridSize*cellX), (int)(offsetY+gridSize*cellY), (int)(gridSize*(cellX+1))-(int)(gridSize*cellX), (int)(gridSize*(cellY+1))-(int)(gridSize*cellY));
                        //render the piece on top
                        char piece = game.grid[i][j];
                        int team;
                        if(!Character.isLetter(piece))
                            continue;
                        team = (Character.isUpperCase(piece)?1:2);
                        int type = -1;
                        switch(Character.toLowerCase(piece)) {
                            case('q'):
                                type = 0;
                                break;
                            case('k'):
                                type = 1;
                                break;
                            case('r'):
                                type = 2;
                                break;
                            case('p'):
                                type = 3;
                                break;
                            case('b'):
                                type = 4;
                                break;
                            case('n'):
                                type = 5;
                                break;
                        }

                        g.drawImage(pieces,
                                (int)(offsetX + cellX * gridSize), (int)(offsetY + cellY * gridSize),
                                (int)(offsetX+(cellX+1)*gridSize), (int)(offsetY+(cellY+1)*gridSize),
                                21*type+1, 21*(team-1)+1, 21*(type+1)-1, 21*team-1, null);
                    }
                }

                if(player1 > -1)
                    game.player1 = JavaClient.javaClient.getClients().get(player1);
                if(player2 > -1)
                    game.player2 = JavaClient.javaClient.getClients().get(player2);

                String readyState = "";
                if(game.player1 != null) {
                    g.setColor(Color.red);
                    g.drawString("Player 1: "+game.player1.getName(),offsetX,offsetY-23);
                    g.drawImage(pieces,offsetX-25,offsetY-40,offsetX-5,offsetY-20,21,0,42,21,null);
                } else
                    readyState = "waiting for players";
                if(game.player2 != null) {
                    g.setColor(Color.blue);
                    g.drawString("Player 2: " + game.player2.getName(), offsetX, offsetY-3);
                    g.drawImage(pieces, offsetX - 25, offsetY - 20, offsetX - 5, offsetY, 21, 21, 42, 42, null);
                } else
                    readyState = "waiting for players";
                
//                System.out.println("well.."+game.player1+":"+player1+","+game.player2+":"+player2);

            if(readyState.equals("")) {
                if(game.winner == -1)
                        readyState = "Stalemate!";
                    else if(game.winner == 1)
                        readyState = "Checkmate! "+game.player1.getName()+" wins!";
                    else if(game.winner == 2)
                        readyState = "Checkmate! "+game.player2.getName()+" wins!";
                    else if(game.turn == 1)
                        readyState = game.player1.getName()+"'s turn";
                    else
                        readyState = game.player2.getName()+"'s turn";
                    if(game.turn == check)
                       readyState += ", check!";
                }

                g.setColor(new Color(0x222222));
                g.drawString(readyState,offsetX+panelSize/2-45,offsetY+panelSize+20);

                int count1 = 0;
                int count2 = 0;
                for(int i=0;i<game.captured.size();i++) {
                    char piece = game.captured.get(i);
                    int team = (Character.isUpperCase(piece)?1:2);
                    int capX = 0;
                    switch(Character.toLowerCase(piece)) {
                        case('q'):
                            capX = 0;
                            break;
                        case('k'):
                            capX = 1;
                            break;
                        case('r'):
                            capX = 2;
                            break;
                        case('p'):
                            capX = 3;
                            break;
                        case('b'):
                            capX = 4;
                            break;
                        case('n'):
                            capX = 5;
                            break;
                    }
                    //display pieces in black
                    int count;
                    if(team == 1)
                        count = count1++;
                    else
                        count = count2++;
                    int posX = count/8;
                    int posY = count%8;
                    double teamX = ((team==2 == !flipped)?offsetX-(posX+1)*gridSize:offsetX+panelSize+posX*gridSize);
                    g.drawImage(pieces,(int)(teamX),(int)(offsetY+posY*gridSize),(int)(teamX+gridSize),(int)(offsetY+(posY+1)*gridSize),
                            capX*21,(team-1)*21,(capX+1)*21,(team)*21,null);
                }
                
                if(promoting) {
                    g.setColor(Color.white);
                    g.fillRect((int)(offsetX+gridSize),(int)(offsetY+gridSize/2),(int)(gridSize*6),(int)(gridSize*2));
                    g.setColor(new Color(0x3388ff));
                    g.drawString("Promote:",(int)(offsetX+gridSize*3),(int)(offsetY+gridSize));
                    g.setColor(new Color(0x1166dd));
                    g.drawRect((int)(offsetX+gridSize*1.25),(int)(offsetY+gridSize*1.25),
                            (int)(gridSize),(int)(gridSize));
                    g.drawRect((int)(offsetX+gridSize*2.75),(int)(offsetY+gridSize*1.25),
                            (int)(gridSize),(int)(gridSize));
                    g.drawRect((int)(offsetX+gridSize*4.25),(int)(offsetY+gridSize*1.25),
                            (int)(gridSize),(int)(gridSize));
                    g.drawRect((int)(offsetX+gridSize*5.75),(int)(offsetY+gridSize*1.25),
                            (int)(gridSize),(int)(gridSize));
                    int type = (playerID == 0 ? 0:1);
                    g.drawImage(pieces,(int)(offsetX+gridSize*1.25),(int)(offsetY+gridSize*1.25),(int)(offsetX+gridSize*(1.25+1)),(int)(offsetY+gridSize*(1.25+1)),
                            0,21*type,21,21*(type+1),null);
                    g.drawImage(pieces,(int)(offsetX+gridSize*2.75),(int)(offsetY+gridSize*1.25),(int)(offsetX+gridSize*(2.75+1)),(int)(offsetY+gridSize*(1.25+1)),
                            21*2,21*type,21*3,21*(type+1),null);
                    g.drawImage(pieces,(int)(offsetX+gridSize*4.25),(int)(offsetY+gridSize*1.25),(int)(offsetX+gridSize*(4.25+1)),(int)(offsetY+gridSize*(1.25+1)),
                            21*4,21*type,21*5,21*(type+1),null);
                    g.drawImage(pieces,(int)(offsetX+gridSize*5.75),(int)(offsetY+gridSize*1.25),(int)(offsetX+gridSize*(5.75+1)),(int)(offsetY+gridSize*(1.25+1)),
                            21*5,21*type,21*6,21*(type+1),null);
                }
            }
        }
        @Override
        public void mouseClicked(MouseEvent mouseEvent){ }

        @Override
        public void mousePressed(MouseEvent mouseEvent){
            if(!isPlaying || game.player2 == null)
                return;

            //To change body of implemented methods use File | Settings | File Templates.
            int mX = mouseEvent.getX();
            int mY = mouseEvent.getY();
            int dX = mX-offsetX;
            int dY = mY-offsetY;

            if(promoting) {
                if(dY < gridSize*1.25 || dY > gridSize*2.25 || dX < gridSize*1.25 || dX > gridSize*(6.75))
                    return;
                
                char piece;
                if(dX > gridSize*1.25 && dX < gridSize*2.25)
                    piece = 'q';
                else if(dX > gridSize*2.75 && dX < gridSize*3.75)
                    piece = 'r';
                else if(dX > gridSize*4.25 && dX < gridSize*5.25)
                    piece = 'b';
                else if(dX > gridSize*5.75 && dX < gridSize*6.75)
                    piece = 'n';
                else
                    return;
                
                if(playerID == 1)
                    piece = Character.toUpperCase(piece);
                
                JavaClient.javaClient.sendMessage("PROMOTE;"+lastMoveX+","+lastMoveY+","+piece);
                promoting = false;
            }
            int cellX = (dX*cells/panelSize);
            int cellY = (dY*cells/panelSize);
            
//            System.out.println(mX+", "+mY+", "+dX+", "+dY+", "+cellX+", "+cellY);

            if(cellX < 0 || cellX >= cells || cellY < 0 || cellY >= cells)
                return;
            
            if(flipped) {
                cellX = 7-cellX;
                cellY = 7-cellY;
            }

            //run the code a second time
            for(int i=0;i<2;i++) {
                if(i == 0 && (selectX == -1 || selectY == -1))
                    continue;
                if(selectX == -1 || selectY == -1) {
                    if(game.turn != playerID)
                        break;
                     if(!Character.isLetter(game.grid[cellY][cellX]))
                        break;
                    if(Character.isUpperCase(game.grid[cellY][cellX]) != (playerID == 1))
                        break;

                    selectX = cellX;
                    selectY = cellY;
                } else {
                    JavaClient.javaClient.sendMessage("MOVE;" + selectX+","+selectY+","+cellX + "," + cellY);
                    boolean breakOut = (selectX == cellX && selectY == cellY);
                    selectX = -1;
                    selectY = -1;
                    if(breakOut)
                        break;
                }
            }
            
            //calculate all valid moves
            if(selectX == -1 || selectY == -1)
                return;
            for(int i=0;i<8;i++)
                for(int j=0;j<8;j++)
                    validMoves[j][i] = game.validMove(selectX,selectY,j,i);
        }
        @Override
        public void mouseReleased(MouseEvent mouseEvent){ }
        @Override
        public void mouseEntered(MouseEvent mouseEvent){ }
        @Override
        public void mouseExited(MouseEvent mouseEvent){ }
    }

    /** a button to allow users to play or spectate
     *
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
     * a button to allow players to leave the service and join the lobby
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
     * a button to allow players who are playing to start a new game
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
     * a checkbox to flip the board 180 degrees
     */
    class FlippedBox extends JCheckBox implements ActionListener {
        public FlippedBox() {
            super();
            addActionListener(this);
        }
        
        public void actionPerformed(ActionEvent event) {
            flipped = isSelected();
        }
    }
}
