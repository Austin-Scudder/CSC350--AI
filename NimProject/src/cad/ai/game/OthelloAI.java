/*******************
 * Christian A. Duncan
 * Modified by: ENTER YOUR NAMES HERE!!!!!
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

/***********************************************************
 * The AI system for a OthelloGame.
 *   Most of the game control is handled by the Server but
 *   the move selection is made here - either via user or an attached
 *   AI system.
 ***********************************************************/
public class OthelloAI extends AbstractAI {
    public OthelloGame game;  // The game that this AI system is playing
    protected Random ran;
    private final int[] rowCheckValues = {1, 1, 0, -1, -1, -1, 0, 1};
    private final int[] columnCheckValues = {0, -1, -1, -1, 0, 1, 1, 1};
    private byte goodVectors = 0b00000000;
    private int tempRow = 0;
    private int tempColumn = 0;
    private char playerPiece = 'O';
    private char opponentPiece = 'X';
    int totalValue = 0;
    
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
	
        char[][] board = (char[][]) game.getStateAsObject();

        // First get the list of possible moves
        int player = game.getPlayer(); // Which player are we?
        ArrayList<OthelloGame.Action> actions = game.getActions(player);
        
        /*
            Best move is:
            
            best of all my options
            
            all of my options are the worst of all their options
            
            all of their options are the best of all my options
            
            
        */

        // Now just pick one of them out at random
        int choice = ran.nextInt(actions.size());
        
        for (OthelloGame.Action action : actions) {
            System.out.println("Calculating move value for " + action.toString());
            System.out.println("Move value for " + action.toString() + ":" + getMoveValue(board, action.row, action.col, player));
        }
        String nextmove = null; 
        int nextmoveval=0;
        for (OthelloGame.Action action : actions) {
            if ( nextmoveval<getMoveValue(board, action.row, action.col, player)){
            	nextmove = action.toString();
            	nextmoveval = getMoveValue(board, action.row, action.col, player);
            	
            }
        }
        
        return nextmove;
    }	
    
    private int getMoveValue(char[][] board, int row, int column, int player) {
        playerPiece = player == 0 ? 'X' : 'O';
        opponentPiece = player == 0 ? 'O' : 'X';
        totalValue = 0;
        /*
            Check east, northeast, north, northwest, west, southwest, south, southeast
        */
        tempRow = 0;
        tempColumn = 0;
        goodVectors = 0b00000000;
        
        for (int i = 0; i < 8; i++) {
            tempRow = row + rowCheckValues[i];
            tempColumn = column + columnCheckValues[i];
            //System.out.println("Evaluating Row: " + tempRow + " and Column: " + (char) (tempColumn + 97));
            /*
                Find good vectors
                    --Find adjacent space with enemy piece
                    --Travel that vector until player piece
                    --If both of the above pass, add to good vector.
            */
            if (!isValidSpace(board, tempRow, tempColumn, opponentPiece)) {
                continue;
            } else while (isValidSpace(board, tempRow, tempColumn, opponentPiece)) {
                //totalValue++;
                //System.out.println("Incrementing move value.");
                tempRow += rowCheckValues[i];
                tempColumn += columnCheckValues[i];
            }
            if (isValidSpace(board, tempRow, tempColumn, playerPiece)) {
                goodVectors |= (byte) (0b00000001 << i);
            }
        }
        for (int i = 0; i < 8; i++) {
            //System.out.println(goodVectors);
            //System.out.println((0b00000001 << i));
            if ((goodVectors & (0b00000001 << i)) == (0b00000001 << i)) {
                tempRow = row + rowCheckValues[i];
                tempColumn = column + columnCheckValues[i];
                //totalValue++;
                while (isValidSpace(board, tempRow, tempColumn, opponentPiece)) {
                    totalValue++;
                    tempRow += rowCheckValues[i];
                    tempColumn += columnCheckValues[i];
                }
            }
        }
        //System.out.println("Cummulative move value: " + totalValue);
        return totalValue;
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
