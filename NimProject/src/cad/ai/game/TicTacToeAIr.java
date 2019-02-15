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
package cad.ai.game;

import java.util.Random;

/***********************************************************
 * The AI system for a TicTacToeGame.
 *   Most of the game control is handled by the Server but
 *   the move selection is made here - either via user or an attached
 *   AI system.
 ***********************************************************/
public class TicTacToeAIr extends AbstractAI {
    public TicTacToeGame game;  // The game that this AI system is playing
    protected Random ran;
    
    public TicTacToeAIr() {
        game = null;
        ran = new Random();
    }

    public synchronized void attachGame(Game g) {
        game = (TicTacToeGame) g;
    }
    
    /**
     * Returns the Move as a String "S"
     *    S=Slot chosen (0-8)
     **/
    public synchronized String computeMove() {
        if (game == null) {
            System.err.println("CODE ERROR: AI is not attached to a game.");
            return "0";
        }
	
        char[] board = (char[]) game.getStateAsObject();

        // First see how many open slots there are
        int openSlots = 0;
        int i = 0;
        for (i = 0; i < board.length; i++) {
            if (board[i] == ' ') { 
            	openSlots++;
            	}
        }
        // Now pick a random open slot
        int s = ran.nextInt(openSlots);

        // And get the proper row
        i = -1;
        while (s >= 0) {
            i++;
            if (board[i] == ' ') s--;  // One more open slot down
        }
        return "" + i;
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
        // This AI probably wants to store (in a file) what
        // it has learned from playing all the games so far...
    }
}
