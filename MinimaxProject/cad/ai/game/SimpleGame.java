/*******************
 * Christian A. Duncan
 * CSC350: Intelligent Systems
 * Spring 2019
 *
 * AI Game Server Project
 * This project is designed to support multiple game platforms to test
 * AI-based solutions.
 * See README file for more details.
 ********************/

package cad.ai.game;

import java.util.Random;

/***********************************************************
 * A simple (guess a number) game.
 *   Two players take turns guessing a number from 1-100.  Each is told
 *   if they are high, low, or correct.
 *   Winner is first to guess correctly - a tie if both get it in same round.
 ***********************************************************/
public class SimpleGame implements Game {
    private static enum State { NOT_GUESSED, HIGH, LOW, CORRECT };
    private int answer;
    private State[] ps;   // The state of most recent guess for each player
    private int turn;    // Whose turn is it 0 or 1.
    private boolean changed;   // Has the state changed (since last transmission)
    
    public SimpleGame() {
        answer = new Random().nextInt(100) + 1;  // Random number from 1 to 100 (inclusive of both)
        ps = new State[2];
        ps[0] = ps[1] = State.NOT_GUESSED;
        changed = true;
    }

    /**
     * Game is only done when every player has had their turn and
     * one of them has guessed correctly.
     **/
    public boolean isDone() {
        return (turn == 0 && (ps[0] == State.CORRECT || ps[1] == State.CORRECT));
    }

    /**
     * Current state of game (in some string format - game dependent)
     * If force is false then a null is returned if nothing has changed since last getState --- 
     * so doesn't repeatedly send the same data...
     **/
    public String getState(boolean force) {
        if (!force && !changed) return null;
        changed = false;
        return turn + "," + toChar(ps[0]) + "," + toChar(ps[1]);
    }	

    private char toChar(State s) {
        return s==State.NOT_GUESSED ? '-' : s==State.HIGH ? 'H' : s==State.LOW ? 'L' : 'C';
    }
    
    /**
     * Get State of the game.  For the AI system.
     * This is an Object but will have to be type-cast after
     * to the proper state based on the Game.
     * This can be more code-friendly than a string.
     **/
    public Object getStateAsObject() {
        return ps;
    }

    /**
     * Update the current state of game (in some string format - game dependent)
     **/
    public void updateState(String state) {
        // TBD...
    }

    /**
     * Get the move from the player or AI.
     * If AI system is in place, query AI else ask player
     **/
    public String getMove() {
        // TBD
        return null;
    }

    /**
     * Process the move requested by the player.
     * p is an integer for the player number.
     *  For two player games, 0=Home, 1=Away...
     * move is a String format for the move - game dependent.
     * Returns a String message to send back to the player.
     **/
    public String processMove(int p, String move) {
        if (p != turn) {
            // Not the player's turn!!!!
            return "It is not your turn.";
        } else {
            try {
                int guess = Integer.parseInt(move);
                turn ^= 1;  // Switch turn from 0 to 1 or 1 to 0
                changed = true;
                if (guess < answer) {
                    ps[p] = State.LOW; return "You guessed low.";
                } else if (guess > answer) {
                    ps[p] = State.HIGH; return "You guess high.";
                } else {
                    ps[p] = State.CORRECT; return "Bingo!";
                }
            } catch (Exception e) {
                return "Could not understand your move.  Please use an integer.";
            }
        }
    }   

    /**
     * Get the winner.  Returns player that won.  
     *   0=Home, 1=Away, -1=Tie, -2=Aborted, -3=Not Finished
     **/
    public int getWinner() {
        if (!isDone()) return -3;
        else if (ps[0] == State.CORRECT) {
            if (ps[1] == State.CORRECT) return -1;
            else return 0;
        } else {
            // There must be a winner... so no need to check
            return 1;
        }
    }

    /**
     * Is it current user's turn?  Based on state information...
     **/
    public boolean isPlayerTurn() {
        return false;  // TBD
    }

    /**
     * Get whose turn it is (0=Home, 1=Away, -1=Nobody yet...)
     **/
    public int getTurn() {
        return 0; // TBD
    }

    /**
     * Get the player's number (0=Home, 1=Away)
     **/
    public int getPlayer() {
        return 0; // TBD
    }

    public void resign(int p) {
        // TBD
    }
    
    /**
     * Post the winner - useful to inform AI if it needs to "learn".
     *   result is either (H)ome win, (A)way win, (T)ie
     **/
    public void postWinner(char result) {
        // TBD
    }

    /**
     * Display the current state.  We'll use a text-based version here.
     **/
    public void displayState() {
        // TBD
    }
    
}
