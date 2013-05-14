package edu.ucsb.cs56.games.client_server;


/**
 * gomoku game is a game object that stores data about a game of gomoku, or 5-in-a-row
 * like other game objects, it provides functionality for sending and receiving game states, as well as win detection
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class GomokuGame{
    public ClientObject player1, player2;

    public int[][] grid;
    public int turn;
    public int winner;
    
    public int cells;

    public GomokuGame() {
        init(9);
    }

    /**
     * initialize as size
     * @param CELLS width and height of game board
     */
    public void init(int CELLS) {
        cells = CELLS;
        grid = new int[cells][cells];
        for(int i=0;i<cells;i++)
            for(int j=0;j<cells;j++)
                grid[i][j] = 0;

        turn = 1;
        winner = 0;
    }

    /**
     * check winner of game
     * @return if the game has a winner
     */
    public boolean checkWinner() {
        for(int i=0;i<cells;i++) {
            for(int j=0;j<cells;j++) {
                int type = grid[i][j];
                if(type == 0)
                    continue;

                //vertical
                if(i < cells-4 && recursiveCheckWin(type, j, i, 0, 4)) {
                    winner = type;
                    return true;
                }
                //horizontal
                if(j < cells-4 && recursiveCheckWin(type, j, i, 2, 4)) {
                    winner = type;
                    return true;
                }
                //down-right
                if(i < cells-4 && j < cells-4 && recursiveCheckWin(type, j, i, 1, 4)) {
                    winner = type;
                    return true;
                }
                //down-left
                if(i < cells-4 && j > 3 && recursiveCheckWin(type, j, i, 3, 4)) {
                    winner = type;
                    return true;
                }
            }
        }
        System.out.println("no winner found");
        return false;
    }

    private boolean recursiveCheckWin(int type, int X, int Y, int dir, int count) {
        if(count < 0) {
            if(X < cells && X >= 0 && Y < cells && Y >= 0 && grid[Y][X] == type) {
                System.out.println("too long");
                return false;
            }

            return true;
        }
        int dx = 1;
        int dy = 1;
        if(dir == 0)
            dx = 0;
        if(dir == 2)
            dy = 0;
        if(dir == 3)
            dx = -1;
        //if the tile before the first tile is of the same type, don't bother
        if(count == 4 && X-dx >=0 && X-dx < cells && Y-dy >=0 && grid[Y-dy][X-dx] == type)
            return false;
        if(grid[Y][X] == type)
            return recursiveCheckWin(type,X+dx,Y+dy,dir,count-1);
        return false;
    }

    /** set state from given string
     *
     * @param data state of the game
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
        String[] rows = info[1].split(";");
        for(int i=0;i<cells;i++) {
            String[] cols = rows[i].split(",");
            for(int j=0;j<cells;j++) {
                grid[i][j] = Integer.parseInt(cols[j]);
            }
        }
        checkWinner();
    }

    /**
     * generate string representing state of the game
     * @return state of the game
     */
    public String getState() {
        String state = "STATE[";
        if(winner == 0)
            state += turn+"]";
        else
            state += "0]";
        for(int i=0;i<cells;i++) {
            for(int j=0;j<cells;j++) {
                state += grid[i][j];
                if(j < cells-1)
                    state +=",";
            }
            if(i<cells-1)
                state += ";";
        }
        return state;
    }
}
