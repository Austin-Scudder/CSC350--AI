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
 * A Tic-Tac-Toe game.
 *   The class game that we all know and love (er, hate?)
 ***********************************************************/
public class TicTacToeGame implements Game {
    private char[] board;  // An array of 9 elements - 'X','O',' ' - for the board.
    private int turn;    // Whose turn is it 0 or 1.
    private int player; // Which "turn" the player is (from Client side)
    private boolean changed;   // Has the state changed (since last transmission)
    private boolean done;
    private int winner = -3;
    private BufferedReader in; // The input to use (when not in AI mode)
    private AI ai;   // AI system
    private int verbose;  // Level of verbosity - currently 0=quiet, >0 output stuff
    
    /** Constructors **/
    public TicTacToeGame() { this(-1, null, null); }
    public TicTacToeGame(int player, BufferedReader in) { this(player, in, null, player == -1, 1); }
    public TicTacToeGame(int player, BufferedReader in, AI ai) { this(player, in, ai, player == -1, 1); }
    public TicTacToeGame(int player, BufferedReader in, AI ai, boolean createFlag, int verbose) {
        this.player = player;
        this.in = in;
        this.ai = ai;
        this.changed = true;
        this.done = false;
        this.winner = -3;
        this.verbose = verbose;
	
        if (createFlag) {
            // Create the game itself (not just get it from another connection)
            // Set up the game
            board = new char[9];
            for (int i = 0; i < board.length; i++) board[i] = ' ';
            turn = 0;  // X goes first
        } else {
            // This is a client version, attached to a game from the server
            turn = -1;       // Don't know whose turn it is yet...
            this.board = null;     // Don't know how the sticks in this game yet.
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
    public boolean computeWinner() {
        // Check if there is a winner along the rows
        for (int r = 0; r < 9; r += 3) {
            char m = board[r];
            if (m != ' ' && m == board[r+1] && m == board[r+2]) {
                winner = (m == 'X') ? 0 : 1;
                done = true;
                return true;
            }
        }

        // Check if there is a winner along the cols
        for (int c = 0; c < 3; c++) {
            char m = board[c];
            if (m != ' ' && m == board[c+3] && m == board[c+6]) {
                winner = (m == 'X') ? 0 : 1;
                done = true;
                return true;
            }
        }

        // What about the diagonals?
        char m = board[4];
        if (m != ' ' && ((m == board[0] && m == board[8]) ||
                         (m == board[2] && m == board[6]))) {
            winner = (m == 'X') ? 0 : 1;
            done = true;
            return true;
        }

        // No winner - but is it a tie?  Could just count number of moves...
        done = false;
        for (int i = 0; i < board.length; i++)
            if (board[i] == ' ') return false;  // Not done yet...

        // It is a tie...
        done = true;
        winner = -1;
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
        String result = turn + "";
        for (int i = 0; i < board.length; i++)
            result += "," + board[i];
        return result;
    }	

    /**
     * Get State of the game.  For the AI system.
     * This is an Object (from Interface) but is actually a char[].
     * Caller should type-cast to this.
     **/
    public synchronized Object getStateAsObject() { return board; }
    
    /**
     * Update the current state of game (in some string format - game dependent)
     **/
    public synchronized void updateState(String state) {
        if (verbose > 0) System.err.println("DEBUG: Updating state: " + state);
        try {
            String[] pieces = state.split(",");  // Break up the state into pieces
            turn = Integer.parseInt(pieces[0]);     // Whose turn is it
            if (board == null) board = new char[pieces.length-1];

            // The markings at each grid location
            for (int i = 0; i < board.length; i++)
                board[i] = pieces[i+1].charAt(0);
	    
            if (verbose > 0) displayState();
        } catch (NumberFormatException e) {
            System.err.println("There was an error in the state that was sent. " + state);
        }
    }

    /**
     * Update the state to the given board.  Copies contents.
     **/
    public synchronized void updateState(char[] newBoard) {
        if (board == null) board = new char[newBoard.length];
        for (int i = 0; i < this.board.length; i++)
            this.board[i] = newBoard[i];
    }
    
    /**
     * Display the current state.  We'll use a text-based version here.
     **/
    public synchronized void displayState() {
        if (board == null) {
            System.out.println("No state yet to display...");
            return;
        }

        // Print the board - no need for fancy loops - just print it out
        System.out.println("   |   |   ");
        System.out.println(" " + board[0] + " | " + board[1] + " | " + board[2]);
        System.out.println("   |   |   ");
        System.out.println("---+---+---");
        System.out.println("   |   |   ");
        System.out.println(" " + board[3] + " | " + board[4] + " | " + board[5]);
        System.out.println("   |   |   ");
        System.out.println("---+---+---");
        System.out.println("   |   |   ");
        System.out.println(" " + board[6] + " | " + board[7] + " | " + board[8]);
        System.out.println("   |   |   ");

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
                return "@ERROR:TicTacToeGame has no AI or BufferedReader attached.  Can't get move!";
            }
            // Get the move from the user
            try {
                System.out.println("What slot do you want?  The slots are numbers 0-8 (across the rows from top-left).");
                String slot = in.readLine();
                // Note, we are not doing any sanity check.  If the user enters wrong info it will be
                // rejected by the Server.  We could though to reduce such user errors...
                turn = -1;  // Avoid asking again until we know whose turn it is
                return "@GAME:MOVE:"+ slot;
            } catch (IOException e) {
                return "@ERROR:IO Error reading in moves.";
            }
        } else {
            // Get the move from the AI
            String move = ai.computeMove();
            if (verbose > 0) System.out.println("AI chose to move " + move);
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
                int slot = Integer.parseInt(move);  // The move is just a single number (0-8)
                if (slot < 0 || slot >= board.length)
                    return  "ERROR:Selection (" + slot + ") is out of range.";
                if (board[slot] != ' ')
                    return "ERROR:This slot is already taken!";
                char symbol = (p == 0) ? 'X' : 'O';
                board[slot] = symbol;
                turn ^= 1;  // Switch turn from 0 to 1 or 1 to 0
                changed = true;
                computeWinner();
                return "MESSAGE:Placed an " + symbol + " in slot " + slot + ".";
            } catch (Exception e) {
                return "ERROR:Could not understand your move.  Please make sure you just pass an integer string (0-8).";
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
     * Get the winner.  Returns player that won.  
     *   0=Home, 1=Away, -1=Tie, -2=Aborted, -3=Not Finished
     **/
    public int getWinner() { return winner; }

    public synchronized void resign(int p) {
        // TBD
    }
    
    /**
     * Post the winner - useful to inform AI if it needs to "learn".
     *   result is either (H)ome win, (A)way win, (T)ie
     **/
    public synchronized void postWinner(char result) {
        if (verbose > 0) {
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
        }

        if (ai != null) ai.postWinner(result);  // Let AI know as well.
    }
}
