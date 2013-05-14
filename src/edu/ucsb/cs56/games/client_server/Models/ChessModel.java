package edu.ucsb.cs56.games.client_server.Models;

import java.util.ArrayList;
import java.util.Arrays;

import edu.ucsb.cs56.games.client_server.ClientObject;

/**
 * Game object for chess game, provides ridiculously complex methods for determining if any move from X1,Y1 to X2,Y1 is
 * a legal, valid move that can be performed without leaving yourself in check
 * takes into account moves like en passant, castling a king, and pawn promotion
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class ChessModel{
    public ClientObject player1, player2;

    public char[][] grid;
    public ArrayList<Character> captured;
    public int turn;
    public int winner;

    //can player 1 or 2 en passant?
    public boolean ep1, ep2;
    //the x coordinate of the most recent en passant opportunity for player 1 or 2
    public int epX1, epX2;
    //
    public boolean castleL1;
    public boolean castleR1;
    public boolean castleL2;
    public boolean castleR2;

    public ChessModel() {
        init();
    }

    /** initializes a new chess game
     *
     */
    public void init() {
        grid = new char[8][8];
        for(int i=2;i<6;i++)
            for(int j=0;j<8;j++)
                grid[i][j] = '0';
        grid[0][0] = 'r';
        grid[0][1] = 'n';
        grid[0][2] = 'b';
        grid[0][3] = 'q';
        grid[0][4] = 'k';
        grid[0][5] = 'b';
        grid[0][6] = 'n';
        grid[0][7] = 'r';

        grid[7][0] = 'R';
        grid[7][1] = 'N';
        grid[7][2] = 'B';
        grid[7][3] = 'Q';
        grid[7][4] = 'K';
        grid[7][5] = 'B';
        grid[7][6] = 'N';
        grid[7][7] = 'R';

        for(int i=0;i<8;i++) {
            grid[1][i] = 'p';
            grid[6][i] = 'P';
        }

        turn = 1;
        winner = 0;
        ep1 = ep2 = true;
        epX1 = epX2 = -1;
        castleL1 = castleL2 = castleR1 = castleR2 = true;
        captured = new ArrayList<Character>();
    }

    /**
     * checks for a winner, and sets winner to the team number if so
     * sets winner to -1 in case of stalemate
     * @return if there is a winner
     */
    public boolean checkWinner() {
        //scan through all possible moves of player, if one results in the king NOT being in check, the game isn't over yet
        //stalemate detection?
        if(isCheckmated(1)) {
            winner = 2;
            return true;
        } else if(isCheckmated(2)) {
            winner = 1;
            return true;
        } else if(isStalemate(turn)) {
            winner = -1;
            return true;
        }
        System.out.println("no winner found");
        return false;
    }

    /**
     * sets the state of the game
     * does NOT yet send data concerning en passant or castling
     * @param data a string of data representing the state of the game
     */
    public void setState(String data) {
        String[] info = data.substring(6).split("]");
        int turnInfo = Integer.parseInt(info[0]);
        if(turnInfo == 0)
            checkWinner();
        else {
            turn = turnInfo;
            winner = 0;
        }
        captured = new ArrayList(Arrays.asList(
                'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R',
                'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P',
                'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r',
                'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'));
        String[] rows = info[1].split(";");
        for(int i=0;i<8;i++) {
            for(int j=0;j<8;j++) {
                grid[i][j] = rows[i].charAt(j);
                Character piece = new Character(grid[i][j]);
                captured.remove(piece);
            }
        }
        checkWinner();
    }

    /**
     * generates a string representing the state of the game
     * @return the state of the game
     */
    public String getState() {
        String state = "STATE[";
        if(winner == 0)
            state += turn+"]";
        else
            state += "0]";
        for(int i=0;i<8;i++) {
            for(int j=0;j<8;j++) {
                state += grid[i][j];
            }
            if(i < 7)
                state += ";";
        }
        return state;
    }

    /**
     * tries a move and, if legal, performs it
     * @param X1 the x coord of the piece to move
     * @param Y1 the y coord of the piece to move
     * @param X2 the new x coord to move to
     * @param Y2 the new y coord to move to
     * @return if the move was successful
     */
    public boolean tryMove(int X1, int Y1, int X2, int Y2) {
        //try the move specified
        //if the turn doesn't match the team 
        char piece = grid[Y1][X1];
        if(!Character.isLetter(piece))
            return false;
        int team = (Character.isUpperCase(piece)?1:2);
        //if upper case, team = 1, lower case team = 2
        int dir = (team==1?-1:1);
        //if team = 1, up, -1, team = 2, down, 1
        if(team != turn)
            return false;
        
        System.out.println("correct team!");

        if(!validMove(X1, Y1, X2, Y2))
            return false;
        System.out.println("valid move!");
        if(Character.toLowerCase(grid[Y1][X1]) == 'k' && Math.abs(X2-X1) > 1) {
            //castling
            grid[Y2][X2] = grid[Y1][X1];
            grid[Y1][X1] = '0';
            if(X2 > X1) {
                grid[Y2][X2-1] = grid[Y2][7];
                grid[Y2][7] = '0';
            } else {
                grid[Y2][X2+1] = grid[Y2][0];
                grid[Y2][0] = '0';
            }
            if(team == 1)
                castleL1 = castleR1 = false;
            else
                castleL2 = castleR2 = false;
        } else if(Character.toLowerCase(grid[Y1][X1]) == 'p' && grid[Y2][X2] == '0' && X2 != X1) {
            //en passant
            grid[Y2][X2] = grid[Y1][X1];
            captured.add(grid[Y1][X2]);
            grid[Y1][X2] = '0';
            grid[Y1][X1] = '0';
            if(team == 1)
                ep1 = false;
            else
                ep2 = false;
        } else {
            //default
            if(Character.isLetter(grid[Y2][X2]))
                captured.add(grid[Y2][X2]);
            grid[Y2][X2] = grid[Y1][X1];
            grid[Y1][X1] = '0';
        }

        //if player moves a rook, invalidate that castle move
        //if player moves a king, invalidate both
        if(team == 1) {
            if(X1 == 0 && Y1 == 7)
                castleL1 = false;
            else if(X1 == 7 && Y1 == 7)
                castleR1 = false;
            else if(Character.toLowerCase(piece)== 'k')
                castleL1 = castleR1 = false;
        } else {
            if(X1 == 0 && Y1 == 0)
                castleL2 = false;
            else if(X1 == 7 && Y1 == 0)
                castleR2 = false;
            else if(Character.toLowerCase(piece) == 'k')
                castleL2 = castleR2 = false;
        }

        //if player moved pawn forward two and opponent can still en passant, allow this
        //otherwise, disallow it
        if(Character.toLowerCase(piece) == 'p' && (team==1?ep2:ep1) && Math.abs(Y2-Y1) > 1) {
            if(team == 1)
                epX2 = X1;
            else
                epX1 = X1;
        } else {
            if(team == 1)
                epX2 = -1;
            else
                epX1 = -1;
        }

        return true;
    }

    //TODO: stalemate resulting from lack of pieces, e.g. two kings
    //checks if the piece at X1Y1 can move to X2Y2 legally
    //accounts for check now by moving the piece and running isInCheck(team)
    //dont forget, en passant, castling, pawn promotion are also considered valid moves (sometimes)
        //check for them here, everything uses validMove

    /**
     * if the move is a valid one, checks to see if its legal and doesn't leave the player in check
     * @param X1 original x coord
     * @param Y1 original y coord
     * @param X2 new x coord
     * @param Y2 new y coord
     * @return if the move is valid
     */
    public boolean validMove(int X1, int Y1, int X2, int Y2) {
        if(X1 < 0 || X1 > 7 || X2 < 0 || X2 > 7 || Y1 < 0 || Y1 > 7 || Y2 < 0 || Y2 > 7)
            return false;
        if(X1 == X2 && Y1 == Y2)
            return false;
        char piece = grid[Y1][X1];
        char victim = grid[Y2][X2];
        //are the piece to move and the victim on the same team?
        if(Character.isLetter(victim) && Character.isUpperCase(piece) == Character.isUpperCase(victim))
            return false;
        if(!Character.isLetter(piece))
            return false;

        //if upper case, team = 1, lower case team = 2
        int team = (Character.isUpperCase(piece)?1:2);
        //if team = 1, up, -1, team = 2, down, 1
        int dir = (team==1?-1:1);

        if(Character.isLetter(victim) && Character.isUpperCase(victim) == Character.isUpperCase(piece))
            return false;

        piece = Character.toLowerCase(piece);

        int dX, dY;
        int dist;
        switch(piece) {
            case('p'):
                if((Y2-Y1)*dir < 1)
                    return false;
                else if((Y2-Y1)*dir > 1) {
                    if(Y1 != 6-(5*(team-1)) || (Y2-Y1)*dir > 2)
                        return false;
                    if(Character.isLetter(victim) || Character.isLetter(grid[Y1+dir][X2]))
                        return false;
                } else
                    if(X2-X1 == 0 && Character.isLetter(victim))
                        return false;
                //some of this might be repetitive, we have NO need to check if the victim and piece are on the same team
                int epX = (team==1?epX1:epX2);
                boolean canEP = (team == 1?ep1:ep2);
                if(X2-X1 != 0) {
                    if(!Character.isLetter(victim) && canEP && epX > -1) {
                        if(X2 != epX || Y2 != (team==1?2:5))
                            return false;
                    } else if(Math.abs(X2-X1) > 1 || !Character.isLetter(victim) || (Character.isUpperCase(victim) == (team == 1)))
                        return false;
                }
                break;
            case('r'):
                dX = X2-X1;
                dY = Y2-Y1;
                if(dX != 0 && dY != 0)
                    return false;
                dist = Math.abs(dX)+Math.abs(dY);
                dX = (dX==0?0:dX/Math.abs(dX));
                dY = (dY==0?0:dY/Math.abs(dY));
                for(int i=1;i<dist;i++)
                    if(Character.isLetter(grid[Y1+dY*i][X1+dX*i]))
                        return false;
                break;
            case('n'):
                dX = Math.abs(X2-X1);
                dY = Math.abs(Y2-Y1);
                if(dX == 0 || dY == 0)
                    return false;
                if(dX+dY != 3)
                    return false;
                if(Character.isLetter(victim) && Character.isUpperCase(victim) == (team == 1))
                    return false;
                break;
            case('b'):
                dX = X2-X1;
                dY = Y2-Y1;
                if(Math.abs(dX) != Math.abs(dY))
                    return false;

                dist = Math.abs(dX);
                dX = (dX==0?0:dX/Math.abs(dX));
                dY = (dY==0?0:dY/Math.abs(dY));
                for(int i=1;i<dist;i++)
                    if(Character.isLetter(grid[Y1+dY*i][X1+dX*i]))
                        return false;
                break;
            case('q'):
                dX = X2-X1;
                dY = Y2-Y1;
                //dX or dY must be 0 or abs dX must equal abs dY
                if(Math.abs(dX) != Math.abs(dY) && dX != 0 && dY != 0)
                    return false;

                dist = (dX==0?Math.abs(dY):Math.abs(dX));
                dX = (dX==0?0:dX/Math.abs(dX));
                dY = (dY==0?0:dY/Math.abs(dY));
                for(int i=1;i<dist;i++)
                    if(Character.isLetter(grid[Y1+dY*i][X1+dX*i]))
                        return false;
                break;
            case('k'):
                dX = X2-X1;
                dY = Y2-Y1;
                if(Math.abs(dY) > 1)
                    return false;
                if(Math.abs(dX) > 1) {
                    if(Math.abs(dX) > 2 || Y1 != Y2)
                        return false;
                    boolean left = (team==1?castleL1:castleL2);
                    boolean right = (team==1?castleR1:castleR2);
                    if(dX > 0) {
                        if(!right)
                            return false;
                        if(Character.isLetter(grid[Y1][X1+1]) || Character.isLetter(grid[Y2][X2]))
                            return false;
                        if(isInCheck(team))
                            return false;
                        grid[Y1][X1+1] = grid[Y1][X1];
                        grid[Y1][X1] = '0';
                        if(isInCheck(team)) {
                            grid[Y1][X1] = grid[Y1][X1+1];
                            grid[Y1][X1+1] = '0';
                            return false;
                        }
                        grid[Y1][X2] = grid[Y1][X1+1];
                        grid[Y1][X1+1] = '0';
                        if(isInCheck(team)) {
                            grid[Y1][X1] = grid[Y1][X2];
                            grid[Y1][X2] = '0';
                            return false;
                        }
                        grid[Y1][X1] = grid[Y1][X2];
                        grid[Y1][X2] = '0';
                    } else {
                        if(!left)
                            return false;
                        if(Character.isLetter(grid[Y1][X1-1]) || Character.isLetter(grid[Y2][X2]) || Character.isLetter(grid[Y2][X1-3]))
                            return false;
                        if(isInCheck(team))
                            return false;
                        grid[Y1][X1-1] = grid[Y1][X1];
                        grid[Y1][X1] = '0';
                        if(isInCheck(team)) {
                            grid[Y1][X1] = grid[Y1][X1-1];
                            grid[Y1][X1-1] = '0';
                            return false;
                        }
                        grid[Y1][X2] = grid[Y1][X1-1];
                        grid[Y1][X1-1] = '0';
                        if(isInCheck(team)) {
                            grid[Y1][X1] = grid[Y1][X2];
                            grid[Y1][X2] = '0';
                            return false;
                        }
                        grid[Y1][X1-3] = grid[Y1][X2];
                        grid[Y1][X2] = '0';
                        if(isInCheck(team)) {
                            grid[Y1][X1] = grid[Y1][X1-3];
                            grid[Y1][X1-3] = '0';
                            return false;
                        }
                        grid[Y1][X1] = grid[Y1][X1-3];
                        grid[Y1][X1-3] = '0';
                    }
                }
                break;
        }

        //if the piece is a king, don't worry about being in check, you've already won!
        if(Character.toLowerCase(victim) == 'k')
            return true;
        boolean check = false;
        grid[Y2][X2] = grid[Y1][X1];
        grid[Y1][X1] = '0';
        System.out.println("is this a valid move for team "+team);
        check = isInCheck(team);
        grid[Y1][X1] = grid[Y2][X2];
        grid[Y2][X2] = victim;

        return !check;
    }

    //consider castling and en passant to be legal moves

    /**
     * whether the player on team team has any legal moves to make
     * @param team the team to check
     * @return if team has legal moves
     */
    public boolean hasLegalMoves(int team) {
        //go through each piece,
        for(int i=0;i<8;i++) {
            for(int j=0;j<8;j++) {
                char piece = grid[i][j];
                //if piece is not a piece or is on opposite team
                if(!Character.isLetter(piece) || Character.isUpperCase(piece) != (team == 1))
                    continue;
                piece = Character.toLowerCase(piece);
                int dir = (team==1?-1:1);
                switch(piece) {
                    //check for en passant here. actually since en passant happens through the normal pawn move, it'll get checked
                    case('p'):
                        if(validMove(j,i,j,i+dir) || validMove(j,i,j-1,i+dir) || validMove(j,i,j+1,i+dir) || validMove(j,i,j,i+2*dir))
                            return true;
                    case('r'):
                        for(int k=0;k<8;k++)
                            if(validMove(j,i,j,k) || validMove(j,i,k,i))
                                return true;
                        break;
                    case('n'):
                        for(int k=-2;k<=2;k+=4)
                            for(int l=-1;l<=1;l+=2)
                                if(validMove(j,i,j+l,i+k) || validMove(j,i,j+k,i+l))
                                    return true;
                        break;
                    case('b'):
                        for(int k=0;k<8;k++) {
                            if(i+(j-k) < 8 && i+(j-k) >= 0 && validMove(j,i,k,i+(j-k)))
                                return true;
                            if(i-(j-k) < 8 && i-(j-k) >= 0 && validMove(j,i,k,i-(j-k)))
                                return true;
                        }
                        break;
                    case('q'):
                        for(int k=0;k<8;k++)
                            if(validMove(j,i,j,k) || validMove(j,i,k,i))
                                return true;
                        for(int k=0;k<8;k++) {
                            if(i+(j-k) < 8 && i+(j-k) >= 0 && validMove(j,i,k,i+(j-k)))
                                return true;
                            if(i-(j-k) < 8 && i-(j-k) >= 0 && validMove(j,i,k,i-(j-k)))
                                return true;
                        }
                        break;
                    case('k'):
                        for(int k=-1;k<=1;k++)
                            for(int l=-1;l<=1;l++)
                                if(validMove(j,i,j+k,i+l))
                                    return true;
                        //try to castle here
                        if(validMove(j,i,j+2,i) || validMove(j,i,j-2,i))
                            return true;
                        break;
                }
            }
        }

        System.out.println("player "+team+" has no legal moves!");
        return false;
    }

    /**
     * if player is checkmated
     * @param team player to check
     * @return if player is checkmated
     */
    public boolean isCheckmated(int team) {
        System.out.println("checking for checkmate..");
        if(isInCheck(team) && !hasLegalMoves(team))
            return true;

        return false;
    }

    /**
     * checks to see if team has legal moves, and if not, the game is a stalemate
     * @param team team whose turn it is
     * @return if the game ends in stalemate
     */
    public boolean isStalemate(int team) {
        System.out.println("checking for stalemate..");
        if(!hasLegalMoves(team) && !isInCheck(team))
            return true;

        return false;
    }

    /**
     * if the player is in check
     * @param team player to test
     * @return if the player is in check
     */
    public boolean isInCheck(int team) {
        System.out.println("is "+team+" in check?");
        int kX = -1;
        int kY = -1;
        char kingChar = (team==1?'K':'k');
        for(int i=0;i<8;i++) {
            for(int j=0;j<8;j++) {
                if(grid[i][j] == kingChar) {
                    kX = j;
                    kY = i;
                    break;
                }
            }
        }
        //cycle through opponent of team's pieces
        //try to move that piece to team's king
        //if it's a valid move, return true for isInCheck
        //else return false

        for(int i=0;i<8;i++) {
            for(int j=0;j<8;j++) {
                if(Character.isUpperCase(grid[i][j]) != (team==1)) {
                    if(validMove(j,i,kX,kY)) {
                        System.out.println("player "+team+" is in check... the "+grid[i][j]+" at "+j+","+i+" can legally move to your king at "+kX+","+kY);
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
