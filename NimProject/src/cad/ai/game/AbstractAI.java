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

/***********************************************************
 * An AbstractAI system that implements the AI interface.
 *   Most of the game control is handled by the Server but
 *   the move selection is made here - either via user or an attached
 *   AI system.
 ***********************************************************/
public abstract class AbstractAI implements AI {
    /**
     * Inform AI who the winner is
     *   result is either (H)ome win, (A)way win, (T)ie
     **/
    public synchronized void postWinner(char result) {
        // Does nothing.  Override if want it to do more.
    }

    /**
     * Shutdown the AI - allowing it to save its learned experience
     **/
    public synchronized void end() {
        // Does nothing.  Override if want it to do more.
    }
}
