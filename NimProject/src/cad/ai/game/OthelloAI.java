/*******************
 * Christian A. Duncan
 * Modified by: Nicholas Molina, Austin Scudder, and Matt Jags
 * CSC350: Intelligent Systems
 * Spring 2019
 *
 * AI Game Client
 * This project is designed to link to a basic Game Server to test
 * AI-based solutions.
 * 
 * OthelloAI:
 *    This class is the main AI system for the Othello game.
 *
 * See README file for more details.
 ********************/
package cad.ai.game;

import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;

/***********************************************************
 * The AI system for a OthelloGame.
 *   Most of the game control is handled by the Server but
 *   the move selection is made here - either via user or an attached
 *   AI system.
 ***********************************************************/
public class OthelloAI extends AbstractAI {
    public static class Action {
        /*
            Borrowing this from OthelloGame. We want to associate a value assessment to an action; the easiest thing is
            just another field.
        */
        public int row;
        public int col;
        public int value = 0;
        public Action(int r, int c) { row = r; col = c; }

        public String toString() { return "" + row + (char) (col+'a'); }
    }
    
    public OthelloGame game;  // The game that this AI system is playing
    protected Random ran;
    private final int[] rowCheckValues = {1, 1, 0, -1, -1, -1, 0, 1};
    private final int[] columnCheckValues = {0, -1, -1, -1, 0, 1, 1, 1};
    private byte goodVectors = 0b00000000;
    private int tempRow = 0;
    private int tempColumn = 0;
    private char playerPiece = 'O';
    private char opponentPiece = 'X';
    private char workingPlayerPiece = 'O';
    private char workingOpponentPiece = 'X';
    private boolean isPlayerInitialized = false;
    private int totalValue = 0;
    private int player = 0;
    private int opponent = 0;
    
    public OthelloAI() {
        game = null;
        ran = new Random();
    }

    public synchronized void attachGame(Game g) {
        game = (OthelloGame) g;
    }
    
    /**
     * Returns the Move as a String "rc" (e.g. 2b)
     **/
    public synchronized String computeMove() {
        if (game == null) {
            System.err.println("CODE ERROR: AI is not attached to a game.");
            return "0a";
        }
        
        if (!isPlayerInitialized) {
            player = game.getPlayer();
            opponent = player ^ 1;
            playerPiece = player == 0 ? 'X' : 'O';
            opponentPiece = player == 0 ? 'O' : 'X';
            isPlayerInitialized = true;
        }
	
        char[][] board = (char[][]) game.getStateAsObject();

        // First get the list of possible moves
        // We're going to use our local version, since we have modified Actions
        ArrayList<Action> actions = getActions(player, board);
        
        /*
            Best move is:
            
            best of all my options
            
            all of my options are the worst of all their options
            
            all of their options are the best of all my options
            
            
        */

        /*
        for (OthelloGame.Action action : actions) {
            System.out.println("Calculating move value for " + action.toString());
            System.out.println("Move value for " + action.toString() + ":" + getMoveValue(board, action.row, action.col, player));
        }
        String nextmove = null; 
        int nextmoveval = 0;
        
        
            
        
        for (OthelloGame.Action action : actions) {
            if (nextmoveval < getMoveTreeValue(board, action.row, action.col, player, 0)) {
            	nextmove = action.toString();
            	nextmoveval = getMoveTreeValue(board, action.row, action.col, player, 0);
            }
        }
        */
        return getBestMove(board, actions).toString();
    }	
    
    private int getMoveValue(char[][] board, int row, int column, int movingPlayer) {
        /*
            It's easiest if the move value always counts up. We adjust what we consider the "player"
            and the "opponent" based on what player we're considering a move for.
            
            Then, at the end, if the moving player is different from the AI's player, then we count the move total as negative.
        */
        if (movingPlayer == player) {
            workingPlayerPiece = playerPiece;
            workingOpponentPiece = opponentPiece;
        } else {
            workingPlayerPiece = opponentPiece;
            workingOpponentPiece = playerPiece;
        }
        totalValue = 0;
        tempRow = 0;
        tempColumn = 0;
        goodVectors = 0b00000000;
        
        for (int i = 0; i < 8; i++) {
            tempRow = row + rowCheckValues[i];
            tempColumn = column + columnCheckValues[i];
            /*
                Find good vectors
                    --Find adjacent space with enemy piece
                    --Travel that vector until player piece
                    --If both of the above pass, add to good vectors.
            */
            if (!isValidSpace(board, tempRow, tempColumn, workingOpponentPiece)) {
                continue;
            } else while (isValidSpace(board, tempRow, tempColumn, workingOpponentPiece)) {
                tempRow += rowCheckValues[i];
                tempColumn += columnCheckValues[i];
            }
            if (isValidSpace(board, tempRow, tempColumn, workingPlayerPiece)) {
                goodVectors |= (byte) (0b00000001 << i);
            }
        }
        for (int i = 0; i < 8; i++) {
            if ((goodVectors & (0b00000001 << i)) == (0b00000001 << i)) {
                tempRow = row + rowCheckValues[i];
                tempColumn = column + columnCheckValues[i];
                while (isValidSpace(board, tempRow, tempColumn, workingOpponentPiece)) {
                    totalValue++;
                    tempRow += rowCheckValues[i];
                    tempColumn += columnCheckValues[i];
                }
            }
        }
        if (movingPlayer == player) {
            return totalValue;
        } else {
            return totalValue * -1;
        }
    }
    
    private int getMoveTreeValue(char[][] workingBoard, int row, int column, int movingPlayer, int totalValue) {
        System.out.println(getActions(player, workingBoard).size());
        if (getActions(player, workingBoard).size() <= 1) {
            System.out.println("we are happy");
            return getMoveValue(workingBoard, row, column, movingPlayer) + totalValue;
        } else if (player == movingPlayer) {
            workingBoard[row][column] = playerPiece;
        } else {
            workingBoard[row][column] = opponentPiece;
        }
        return getMoveTreeValue(workingBoard, row, column, 1 ^ movingPlayer, totalValue);
    }
    
    private Action getBestMove(char[][] workingBoard, ArrayList<Action> actions) {
        Action bestAction = actions.get(0);
        if (actions.size() == 1) {
            bestAction.value += getMoveValue(workingBoard, bestAction.row, bestAction.col, player);
        } else {
            char[][] tempBoard = copyBoard(workingBoard);
            tempBoard[bestAction.row][bestAction.col] = playerPiece;
            if (getActions(opponent, tempBoard).size() > 0) {
                bestAction.value += getWorstMove(tempBoard, getActions(opponent, tempBoard)).value;
            }
            for (int i = 1; i < actions.size(); i++) {
                tempBoard = copyBoard(workingBoard);
                tempBoard[actions.get(i).row][actions.get(i).col] = playerPiece;
                if (getActions(opponent, tempBoard).size() > 0) {
                    actions.get(i).value += getWorstMove(tempBoard, getActions(opponent, tempBoard)).value;
                }
                if (bestAction.value < actions.get(i).value) {
                    bestAction = actions.get(i);
                }
            }
        }
        return bestAction;
    }
    
    private Action getWorstMove(char[][] workingBoard, ArrayList<Action> actions) {
        Action worstAction = actions.get(0);
        if (actions.size() == 1) {
            worstAction.value += getMoveValue(workingBoard, worstAction.row, worstAction.col, opponent);
        } else {
            char[][] tempBoard = copyBoard(workingBoard);
            tempBoard[worstAction.row][worstAction.col] = opponentPiece;
            if (getActions(player, tempBoard).size() > 0) {
                worstAction.value += getBestMove(tempBoard, getActions(player, tempBoard)).value;
            }
            for (int i = 1; i < actions.size(); i++) {
                tempBoard = copyBoard(workingBoard);
                tempBoard[actions.get(0).row][actions.get(0).col] = opponentPiece;
                if (getActions(player, tempBoard).size() > 0) {
                    actions.get(i).value += getBestMove(tempBoard, getActions(player, tempBoard)).value;
                }
                if (worstAction.value < actions.get(0).value) {
                    worstAction = actions.get(0);
                }
            }
        }
        return worstAction;
    }
    
    private char[][] copyBoard(char[][] board) {
        char[][] newBoard = new char[board.length][board.length]; // This only works because we know the board is square.
        for (int i = 0; i < board.length; i++) { // Likewise
            newBoard[i] = Arrays.copyOf(board[i], board.length);
        }
        return newBoard;
    }
    
    /*
        These next three are just c/ped and modified from OthelloGame.java, so Professor, thank yourself.
        
        In short, we need to do all these operations on our own deep copy of the board. All of these functions
        in OthelloGame work with the current actual board state. We need our own board for hypotheticals.
        
        We could create a new OthelloGame and modify that. However, copying an array is immensely less overhead than
        creating an entirely new object; by definition, creating a new OthelloGame will include, at the least, copying
        the current board. In the interest of speed, we're using the same functions OthelloGame does, but modified to
        consider a hypothetical board. Since they already consider a board object, just adding in the board as an 
        additional argument does what we need.
    */
    
    /**
     * Get the various (valid) actions that are possible 
     * @param player - which player is moving
     * @return The Actions possible.
     **/
    public ArrayList<Action> getActions(int player, char[][] board) {
        ArrayList<Action> result = new ArrayList<Action>();
        char symbol = (player == 0) ? 'X' : 'O';
	
        // Go through EVERY spot on the board
        for (int r = 0; r < board.length; r++)
            for (int c = 0; c < board[r].length; c++)
                // Determine if this board location yields a valid move
                if (isValidMove(symbol, r, c, board))
                    result.add(new Action(r,c));

        return result;
    }
    
     /**
     * Is this board location a valid move?
     * @param symbol The symbol to place X or O
     * @param row The row to place symbol
     * @param col The col to place symbol
     * @returns True if this location creates at least ONE flip option.
     **/
    public boolean isValidMove(char symbol, int row, int col, char[][] board) {
        return
            board[row][col] == ' ' &&       // Space is open
            (flipDirection(symbol, row, col, -1,  0, false, board) ||   // NORTH
             flipDirection(symbol, row, col, +1,  0, false, board) ||   // SOUTH
             flipDirection(symbol, row, col,  0, -1, false, board) ||   // WEST
             flipDirection(symbol, row, col,  0, +1, false, board) ||   // EAST
             flipDirection(symbol, row, col, -1, -1, false, board) ||   // NW
             flipDirection(symbol, row, col, -1, +1, false, board) ||   // NE
             flipDirection(symbol, row, col, +1, -1, false, board) ||   // SW
             flipDirection(symbol, row, col, +1, +1, false, board));
    }

    /**
     * Try to flip the pieces in the given direction
     * @param flip True if it should FLIP, False if it should just SEE if it can flip
     **/
    private boolean flipDirection(char symbol, int row, int col, int dr, int dc, boolean flip, char[][] board) {
        int r, c, count;
        boolean flipped = false;
        for (r = row+dr, c = col+dc, count = 0; r >= 0 && r < board.length && c >= 0 && c < board.length && board[r][c] != ' ';
             r += dr, c += dc, count++) {
            if (board[r][c] == symbol) {
                // Found one
                if (count > 0 && flip) 
                    // Flip the ones over between the two (if any!)
                    for (r -= dr, c -= dc; r != row || c != col; r -= dr, c -= dc) {
                        board[r][c] = symbol;
                    }
                return count > 0;
            }
        }
        return false;
    }
 
    
    private boolean isValidSpace(char[][] board, int row, int column, char desiredPiece) {
        if ((row >= board.length) || (column >= board.length) || (row < 0) || (column < 0)) {
            return false;
        }
        if (board[row][column] != desiredPiece) {
            return false;
        }
        return true;
    }

    /**
     * Inform AI who the winner is
     *   result is either (H)ome win, (A)way win, (T)ie
     **/
    @Override
    public synchronized void postWinner(char result) {
        // This AI probably wants to store what it has learned
        // about this particular game.
        game = null;  // No longer playing a game though.
    }

    /**
     * Shutdown the AI - allowing it to save its learned experience
     **/
    @Override
    public synchronized void end() {
        // Probably don't need anything here at the moment since it isn't LEARNING anything
    }
}
