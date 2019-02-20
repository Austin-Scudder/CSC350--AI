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

        // Now just pick one of them out at random
        int choice = ran.nextInt(actions.size());
        return actions.get(choice).toString();
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
