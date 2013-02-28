package edu.ucsb.cs56.W12.jcolicchio.issue535;

/**
 * tictactoegame is a tic tac toe game object that stores data about a tic tac toe game, such as placement of Xs and Os,
 * and possibly the winner
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class TicTacToeGame{
    public ClientObject player1, player2;
    
    public int[][] grid;
    public int turn;
    public int winner;

    public TicTacToeGame() {
        init();
    }

    public void init() {
        grid = new int[3][3];
        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++)
                grid[i][j] = 0;

        turn = 1;
        winner = 0;
    }

    public boolean checkWinner() {
        for(int j=1;j<3;j++) {
            for(int i=0;i<3;i++) {
                if(grid[0][i] == j && grid[1][i] == j && grid[2][i] == j) {
                    winner = j;
                    return true;
                }
                if(grid[i][0] == j && grid[i][1] == j && grid[i][2] == j) {
                    winner = j;
                    return true;
                }
            }
            if(grid[0][0] == j && grid[1][1] == j && grid[2][2] == j) {
                winner = j;
                return true;
            }
            if(grid[0][2] == j && grid[1][1] == j && grid[2][0] == j) {
                winner = j;
                return true;
            }
        }
        System.out.println("no winner found");
        return false;
    }
    
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
        for(int i=0;i<3;i++) {
            String[] cols = rows[i].split(",");
            for(int j=0;j<3;j++) {
                grid[i][j] = Integer.parseInt(cols[j]);
            }
        }
        checkWinner();
    }
    
    public String getState() {
        String state = "STATE[";
        if(winner == 0)
            state += turn+"]";
        else
            state += "0]";
        for(int i=0;i<3;i++) {
            for(int j=0;j<3;j++) {
                state += grid[i][j];
                if(j < 2)
                    state +=",";
            }
            if(i<2)
                state += ";";
        }
        return state;
    }
}
