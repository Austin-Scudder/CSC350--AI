/*******************
 * Christian A. Duncan
 * CSC350: Intelligent Systems
 * Spring 2019
 *
 * AI Game Client
 * This project is designed to link to a basic Game Server to test
 * AI-based solutions.
 * See README file for more details.
 ********************/

package cad.ai.solutions;

import java.util.ArrayList;
import java.io.*;
import cad.ai.game.*;

/***********************************************************
 * The AI system for a TicTacToeGame.
 *   Most of the game control is handled by the Server but
 *   the move selection is made here - either via user or an attached
 *   AI system.
 *   This AI program uses the Minimax algorithm to determine the 
 *   optimal move.
 ***********************************************************/
public class TicTacToeAIMinimax extends cad.ai.game.TicTacToeAI {
    TicTacToeGame practiceGame;   // A temporary game state
    
    /**
     * Basic constructor.  Nothing additional to do than basic AI
     **/ 
    public TicTacToeAIMinimax() {
        super();
        practiceGame = new TicTacToeGame(-1, null, null, false, 0);
    }
    
    /**
     * Returns the Move as a String "S"
     *    S=Slot chosen (0-8)
     **/
    @Override
    public synchronized String computeMove() {
        if (game == null) {
            System.err.println("CODE ERROR: AI is not attached to a game.");
            return "0";
        }

        char[] board = (char[]) game.getStateAsObject();

        // Determine Maximum score among all opponent's options (max of min)
        Integer bestAction = null;
        int bestScore = Integer.MIN_VALUE; // Negative "infinity"
        for (Integer a: getActions(board)) {
            int score = minValue(result(board, a, game.getPlayer()));
            if (score > bestScore) {
                bestAction = a;
                bestScore = score;
            }
        }
	
        System.out.println("Choosing Action:" + bestAction +
                           ", Score:" + bestScore);
        return bestAction.toString();
    }

    /**
     * Return all the actions with this board
     * @param: The board configuration.
     **/
    private Iterable<Integer> getActions(char[] board) {
        ArrayList<Integer> res = new ArrayList<Integer>(10);
        for (int i = 0; i < board.length; i++)
            if (board[i] == ' ') res.add(new Integer(i));
        return res;
    }

    /**
     * Result of applying the given "action" to the board state.
     * @param board: The original board state
     * @param action: The "move" to apply
     * @param player: Which player is making the move? Home (0) or Away (1)
     * @return A copy of the board AFTER applying given action by player.
     **/
    private char[] result(char[] board, Integer action, int player) {
        char[] res = (char[]) board.clone();
        res[action.intValue()] = (player == 0 ? 'X' : 'O');
        return res;
    }
    
    /**
     * Home wishes to MAXimize the score.
     * @param: The board state to determine maximum move
     **/
    private int maxValue(char[] board) {
        // Is this a terminal board
        practiceGame.updateState(board);
        if (practiceGame.computeWinner()) {
            // We have a winner - return its Utility
            //   1 for Player wins, -1 for Player loses, 0 for Tie
            int w = practiceGame.getWinner();
            return w < 0 ? 0 : w == game.getPlayer() ? 1 : -1;
        }
	
        // Determine Maximum value among all possible actions
        int bestScore = Integer.MIN_VALUE; // Negative "Infinity"
        for (Integer a: getActions(board)) {
            int score = minValue(result(board, a, game.getPlayer()));
            if (score > bestScore) bestScore = score;
        }
        return bestScore;
    }

    /**
     * Away wishes to MINimize the score.
     * @param: The board state to determine minimum move
     **/
    private int minValue(char[] board) {
        // Is this a terminal board
        practiceGame.updateState(board);
        if (practiceGame.computeWinner()) {
            // We have a winner - return its Utility
            //   1 for Player wins, -1 for Player loses, 0 for Tie
            int w = practiceGame.getWinner();
            return w < 0 ? 0 : w == game.getPlayer() ? 1 : -1;
        }
	
        // Determine Maximum value among all possible actions
        int bestScore = Integer.MAX_VALUE; // Positive "Infinity"
        for (Integer a: getActions(board)) {
            int score = maxValue(result(board, a, 1-game.getPlayer()));
            if (score < bestScore) bestScore = score;
        }
        return bestScore;
    }
    
    /**
     * Inform AI who the winner is
     *   result is either (H)ome win, (A)way win, (T)ie
     **/
    @Override
    public synchronized void postWinner(char result) {
        // We don't care.  Our AI does not LEARN!
    }

    /**
     * Shutdown the AI - allowing it to save its learned experience
     **/
    @Override
    public synchronized void end() {
        // We don't care.  Our AI does not LEARN!
    }
}
