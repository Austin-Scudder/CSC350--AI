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
import java.io.BufferedReader;
import java.io.IOException;

/***********************************************************
 * A Nim game.
 *   Two players take turns removing as many sticks as they want from any row.
 *   The player removing the last stick wins.
 *   The number of rows will vary from MIN_ROW to MAX_ROW (inclusive of both)
 *   The number of sticks per row will vary from MIN_STICK to MAX_STICK (inclusive of both)
 ***********************************************************/
public class NimGame implements Game {
    private static final int MIN_ROW = 3;
    private static final int MAX_ROW = 10;
    private static final int MIN_STICK = 5;
    private static final int MAX_STICK = 100;
    
    private int[] sticks;  // Amount of sticks per row...
    private int turn;    // Whose turn is it 0 or 1.
    private int player; // Which "turn" the player is (from Client side)
    private boolean changed;   // Has the state changed (since last transmission)
    private boolean done;
    private int winner = -3;
    private BufferedReader in; // The input to use (when not in AI mode)
    private NimAI ai;   // AI system

    /** Constructors **/
    public NimGame() { this(-1, null, null); }
    public NimGame(int player, BufferedReader in) { this(player, in, null); }
    public NimGame(int player, BufferedReader in, AI ai) { this(player, in, ai, player == -1); }
    public NimGame(int player, BufferedReader in, AI ai, boolean createFlag) {
        this.player = player;
        this.in = in;
        this.ai = (NimAI) ai;
        this.changed = true;
        this.done = false;
        this.winner = -3;
        if (createFlag) {
            // Create the game itself (not just get it from another connection)
            // Set up the game
            Random ran = new Random();
            int rows = ran.nextInt(MAX_ROW - MIN_ROW + 1) + MIN_ROW;
            this.sticks = new int[rows];
            for (int i = 0; i < rows; i++)
                this.sticks[i] = ran.nextInt(MAX_STICK - MIN_STICK + 1) + MIN_STICK;
            this.turn = 0;
        } else {
            // This is a client version, attached to a game from the server
            this.turn = -1;       // Don't know whose turn it is yet...
            this.sticks = null;     // Don't know how the sticks in this game yet.
        } 
        if (this.ai != null) {
            // Let the AI know what game she is playing...
            ai.attachGame(this);
        }
    } 

    /**
     * Game is only done when every player has had their turn and
     * one of them has guessed correctly.
     **/
    public boolean isDone() { return done; }

    // Done internally since checked after every move - no need to do it all the time.
    private boolean isDoneCheck() {
        done = false;
        for (int i = 0; i < sticks.length; i++)
            if (sticks[i] > 0) return false;  // A row without 0 sticks, not done yet.
        done = true;
        return true;  // All rows have 0 sticks
    }

    /**
     * Current state of game (in some string format - game dependent)
     * If force is false then a null is returned if nothing has changed since last getState --- 
     * so doesn't repeatedly send the same data...
     **/
    public String getState(boolean force) {
        if (!force && !changed) return null;
        changed = false;
        String result = turn + "," + sticks.length;
        for (int i = 0; i < sticks.length; i++)
            result += "," + sticks[i];
        return result;
    }	

    /**
     * Get State of the game.  For the AI system.
     * This is an Object (from Interface) but is actually an int[].
     * Caller should type-cast to this.
     **/
    public synchronized Object getStateAsObject() { return sticks; }
    
    /**
     * Update the current state of game (in some string format - game dependent)
     **/
    public synchronized void updateState(String state) {
        try {
            String[] pieces = state.split(",");  // Break up the state into pieces
            this.turn = Integer.parseInt(pieces[0]);     // Whose turn is it
            int numRows = Integer.parseInt(pieces[1]);   // How many rows are there?
            if (this.sticks == null)
                this.sticks = new int[numRows];
            else if (this.sticks.length != numRows) {
                System.err.println("Number of rows does not MATCH!");
                return;
            }
            // How many sticks left in each row
            for (int i = 0; i < numRows; i++)
                this.sticks[i] = Integer.parseInt(pieces[i+2]);
	    
            displayState();
        } catch (NumberFormatException e) {
            System.err.println("There was an error in the state that was sent. " + state);
        }
    }

    /**
     * Display the current state.  We'll use a text-based version here.
     **/
    public synchronized void displayState() {
        if (sticks == null) {
            System.out.println("No state yet to display...");
            return;
        }

        // Print the rows of sticks
        for (int i = 0; i < sticks.length; i++)
            System.out.println(i + ": " + sticks[i]);

        // And whose turn it is...
        System.out.println("Turn = " + ((turn == 0) ? "Home" : "Away") +
                           "(" + ((turn == player) ? "You" : "Opponent") + ")");
    }

    /**
     * Get the move from the player or AI.
     * If AI system is in place, query AI else ask player
     **/
    public synchronized String getMove() {
        if (turn != player) {
            System.err.println("DEBUG: It isn't the player's turn yet!");
            return null;
        } else if (ai == null) {
            if (in == null) {
                return "@ERROR:NimGame has no AI or BufferedReader attached.  Can't get move!";
            }
            // Get the move from the user
            try {
                System.out.println("What row do you want select?");
                String row = in.readLine();
                System.out.println("How many sticks to remove from that row?");
                String sticks = in.readLine();
                // Note, we are not doing any sanity check.  If the user enters wrong info it will be
                // rejected by the Server.  We could though to reduce such user errors...
                turn = -1;  // Avoid asking again until we know whose turn it is
                return "@GAME:MOVE:"+ row + "," + sticks;
            } catch (IOException e) {
                return "@ERROR:IO Error reading in moves.";
            }
        } else {
            // Get the move from the AI
            String move = ai.computeMove();
            System.out.println("AI chose to move " + move);
            turn = -1;  // Avoid asking again until we know whose turn it is
            return("@GAME:MOVE:"+move);
        }
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
            return "ERROR:It is not your turn.";
        } else {
            try {
                String[] split = move.split(",", 2);  // Just two things should be given for move.
                int row = Integer.parseInt(split[0]);
                int take = Integer.parseInt(split[1]);
                if (row < 0 || row >= sticks.length)
                    return  "ERROR:Row is out of range.";
                if (take <= 0)
                    return "ERROR:You must take a positive number of sticks.";
                if (sticks[row] < take)
                    return "ERROR:That row does not have that many sticks.";
                sticks[row] -= take;
                turn ^= 1;  // Switch turn from 0 to 1 or 1 to 0
                changed = true;
                if (sticks[row] == 0 && isDoneCheck()) {
                    winner = (turn^1);
                    return "MESSAGE:" + (winner == 0 ? "Home" : "Away") + " won!";
                } else
                    return "MESSAGE:Took " + take + " sticks from row " + row + ".";
            } catch (Exception e) {
                return "ERROR:Could not understand your move.  Please use Row,Take as integral values.";
            }
        }
    }   

    /**
     * Is it current user's turn?  Based on state information...
     **/
    public synchronized boolean isPlayerTurn() { return turn==player; }

    /**
     * Get whose turn it is (0=Home, 1=Away, -1=Nobody yet...)
     **/
    public int getTurn() { return turn; }

    /**
     * Get the player's number (0=Home, 1=Away)
     *  -1 is returned if this is the Server version which is not attached to a specific player.
     **/
    public synchronized int getPlayer() { return player; }

    /**
     * Player p resigns - due to forfeiting for example.
     *  Too much time or too many wrong attempts in a row.
     **/
    public synchronized void resign(int p) {
        winner = 1-p;  // Winner is the other player
        done = true;
	
        // Clear the sticks (for good measure)
        for (int i = 0; i < sticks.length; i++) sticks[i] = 0;
    }
    
    /**
     * Get the winner.  Returns player that won.  
     *   0=Home, 1=Away, -1=Tie, -2=Aborted, -3=Not Finished
     **/
    public int getWinner() { return winner; }

    /**
     * Post the winner - useful to inform AI if it needs to "learn".
     *   result is either (H)ome win, (A)way win, (T)ie
     **/
    public synchronized void postWinner(char result) {
        switch (result) {
        case 'T':
            System.out.println("It was a TIE!"); break;
        case 'H':
            System.out.println((player == 0) ? "You won!" : "You lost."); break;
        case 'A':
            System.out.println((player == 1) ? "You won!" : "You lost."); break;
        default:
            System.out.println("Unrecognized winner.");
        }

        if (ai != null) ai.postWinner(result);  // Let AI know as well.
    }
}
