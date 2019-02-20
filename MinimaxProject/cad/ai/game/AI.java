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
 * An interface to the AI system for a generic Game.
 ***********************************************************/
public interface AI {
    /**
     * Register the game the AI should currently be playing.
     **/
    public void attachGame(Game g);
    
    /**
     * Returns the Move as a String (depends on the game being played)
     **/
    public String computeMove();

    /**
     * Inform AI who the winner is
     *   result is either (H)ome win, (A)way win, (T)ie
     *   Useful if the AI wishes to "learn" from this game.
     **/
    public void postWinner(char result);

    /**
     * Shutdown the AI - allowing it to save its learned experience
     **/
    public void end();
}
