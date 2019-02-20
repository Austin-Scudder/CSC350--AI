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
import java.util.ArrayList;

/***********************************************************
 * An Othello game (aka Reversi)
 *   Classic challenge...
 ***********************************************************/
public class OthelloGame implements Game {
    public static class Action {
        public int row;
        public int col;
        public Action(int r, int c) { row = r; col = c; }

        public String toString() { return "" + row + (char) (col+'a'); }
    }
    
    private char[][] board;  // A grid of X's and O's and spaces.
    private int turn;    // Whose turn is it 0 or 1.
    private int player; // Which "turn" the player is (from Client side)
    private boolean changed;   // Has the state changed (since last transmission)
    private boolean done;
    private int winner = -3;
    private BufferedReader in; // The input to use (when not in AI mode)
    private AI ai;   // AI system
    private int verbose;  // Level of verbosity - currently 0=quiet, >0 output stuff
    private int homeScore = -1;
    private int awayScore = -1;

    /** Constructors **/
    public OthelloGame() { this(-1, null, null); }
    public OthelloGame(int player, BufferedReader in) { this(player, in, null, player == -1, 1); }
    public OthelloGame(int player, BufferedReader in, AI ai) { this(player, in, ai, player == -1, 1); }
    public OthelloGame(int player, BufferedReader in, AI ai, boolean createFlag, int verbose) {
        this(player, in, ai, createFlag, verbose, 8, 8);
    }
    
    public OthelloGame(int player, BufferedReader in, AI ai, boolean createFlag, int verbose, int dimension) { this(player, in, ai, createFlag, verbose, dimension, dimension); }
    
    public OthelloGame(int player, BufferedReader in, AI ai, boolean createFlag, int verbose, int dimensionA, int dimensionB) { 
        this.player = player;
        this.in = in;
        this.ai = ai;
        this.changed = true;
        this.done = false;
        this.winner = -3;
        this.verbose = verbose;

        if (createFlag) {
            if (dimensionA <= 2 || dimensionB <= 2) { // || dimension %2 != 0) {
                System.err.println("PROG ERROR: Dimension must be even and bigger than 2, using default of 8x8");
                dimensionA = dimensionB = 8;
            }
	
            // Create the game itself (not just get it from another connection)
            // Set up the game
            board = new char[dimensionA][dimensionB];
            for (int r = 0; r < board.length; r++)
                for (int c = 0; c < board[r].length; c++)
                    board[r][c] = ' ';

            // Place the initial 4 pieces
            int midA = dimensionA/2;
            int midB = dimensionB/2;
            board[midA][midB] = 'O';
            board[midA-1][midB-1] = 'O';
            board[midA][midB-1] = 'X';
            board[midA-1][midB] = 'X';
	    
            turn = 0;  // X goes first
        } else {
            // This is a client version, attached to a game from the server
            turn = -1;       // Don't know whose turn it is yet...
            this.board = null;     // Don't know the board state yet.
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
    public synchronized boolean computeWinner() {
        // First determine if the game is done (when no more moves are possible for either team)
        ArrayList<Action> actionList = getActions(turn);
        if (actionList.size() > 0) {
            // There are actions available
            return false;
        } else {
            // Try it for the other player
            turn ^= 1;
            actionList = getActions(turn);
            if (actionList.size() > 0) {
                // There are actions available
                return false;
            } else {
                // Neither player can move.
                done = true;
		
                // The game is over.  Determine who won.
                computeScore();
                if (homeScore > awayScore) winner = 0;  // Home won
                else if (homeScore < awayScore) winner = 1; // Away won
                else winner = -1;  // A tie
                return true;
            }
        }
    }

    /**
     * Compute the score of the two opponents - at current board 
     **/
    public synchronized void computeScore() { computeScore(-1); }

    /**
     * Compute the score of the two opponents.
     * If it is a forfeit, forfeit indicates which player "won"
     **/
    public synchronized void computeScore(int forfeit) {
        homeScore = 0;
        awayScore = 0;
        int emptyScore = 0;
        for (int r = 0; r < board.length; r++)
            for (int c = 0; c < board[r].length; c++)
                if (board[r][c] == 'X')
                    homeScore++;
                else if (board[r][c] == 'O')
                    awayScore++;
                else
                    emptyScore++;  // Empty Slots
        if (forfeit == 0) {
            homeScore += emptyScore;  // Home gets empty slots
            if (homeScore < awayScore) homeScore = awayScore+1;
        } else if (forfeit == 1) {
            awayScore += emptyScore; // Away gets empty slots
            if (awayScore < homeScore) awayScore = homeScore+1;
        } else {
            // Winner gets the empty slots (Not including - not necessary!)
            // if (homeScore > awayScore) homeScore += emptyScore;
            // else if (awayScore > homeScore) awayScore += emptyScore;
        }
    }
    
    /**
     * Current state of game (in some string format - game dependent)
     * If force is false then a null is returned if nothing has changed since last getState --- 
     * so doesn't repeatedly send the same data...
     **/
    public synchronized String getState(boolean force) {
        if (!force && !changed) return null;
        changed = false;
        String result = turn + "," + board.length + "," + board[0].length;
        for (int r = 0; r < board.length; r++)
            for (int c = 0; c < board[0].length; c++)
                result += "," + board[r][c];
        return result;
    }	

    /**
     * Get State of the game.  For the AI system.
     * This is an Object (from Interface) but is actually a char[][].
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
            int dimA = Integer.parseInt(pieces[1]); // Number of rows in grid
            int dimB = Integer.parseInt(pieces[2]); // Number of cols in grid
            if (board == null || board.length != dimA || board[0].length != dimB)
                // Not the same board dimension as we had.  Make a new one.
                board = new char[dimA][dimB];

            // The markings at each grid location
            for (int i = 3, r = 0; r < board.length; r++)
                for (int c = 0; c < board[0].length; c++, i++)
                    board[r][c] = pieces[i].charAt(0);
	    
            if (verbose > 0) displayState();
        } catch (NumberFormatException e) {
            System.err.println("There was an error in the state that was sent. " + state);
        }
    }

    /**
     * Update the state to the given board.  Clones contents.
     **/
    public synchronized void updateState(int turn, char[][] newBoard) {
        board = new char[newBoard.length][];
        for (int i = 0; i < this.board.length; i++)
            this.board[i] = newBoard[i].clone();
        this.turn = turn;
    }
    
    /**
     * Display the current state.  We'll use a text-based version here.
     **/
    public synchronized void displayState() {
        if (board == null) {
            System.out.println("No state yet to display...");
            return;
        }

        // Print the column header
        System.out.print(" ");
        for (int c = 0; c < board[0].length; c++)
            System.out.print(" " + (char) (c+'a'));
        System.out.println();

        // Print each row (with row header)
        for (int r = 0; r < board.length; r++) {
            System.out.print(r);
            for (int c = 0; c < board[r].length; c++)
                System.out.print(" " + board[r][c]);
            System.out.println(" " + r);
        }

        // Print the column header
        System.out.print(" ");
        for (int c = 0; c < board[0].length; c++)
            System.out.print(" " + (char) (c+'a'));
        System.out.println();

        // Display Current Score and whose turn it is...
        computeScore();
        System.out.println("Score: X=" + homeScore + " O="+ awayScore);
        System.out.println("Turn: " + ((turn == 0) ? "Home (X)" : "Away (O)"));
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
                return "@ERROR:OthelloGame has no AI or BufferedReader attached.  Can't get move!";
            }

            // Get the move from the user
            try {
                System.out.println("What location do you want to choose? Use rc.  E.g. 2a means row 2 column a.");
                String move = in.readLine();
                // Note, we are not doing any sanity check.  If the user enters wrong info it will be
                // rejected by the Server.  We could though to reduce such user errors...
                turn = -1;  // Avoid asking again until we know whose turn it is
                return "@GAME:MOVE:"+ move;
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
                // Check if the move is actually empty
                int row = move.charAt(0) - '0';
                if (row < 0 || row >= board.length)
                    return "ERROR: Invalid move (incorrect row): " + move;
                int col = move.charAt(1) - 'a';
                if (col < 0 || col >= board[row].length)
                    return "ERROR: Invalid move (incorrect column): " + move;
                if (board[row][col] != ' ')
                    return "ERROR: This location is already taken!";
		
                // Process move (internally)
                if (!processMove(p, row, col))
                    return "ERROR: Invalid move: " + move;
                changed = true;
                turn ^= 1;   // Switch turn from 0 to 1 or 1 to 0
                computeWinner();
                return "MESSAGE:Placed mark at location " + move + ".";
            } catch (Exception e) {
                return "ERROR:Could not understand your move.  Please make sure you just pass an integer string (0-8).";
            }
        }
    }   

    /**
     * Place piece at provided location
     **/
    public boolean processMove(int p, int row, int col) {
        char symbol = (p == 0) ? 'X' : 'O';
	
        if (board[row][col] != ' ') return false;  // Spot not available
        boolean flipped = false;  // Have we flipped ANY pieces?

        // Check if we can flip in every one of the 8 directions
        flipped |= flipDirection(symbol, row, col, -1,  0, true);   // NORTH
        flipped |= flipDirection(symbol, row, col, +1,  0, true);   // SOUTH
        flipped |= flipDirection(symbol, row, col,  0, -1, true);   // WEST
        flipped |= flipDirection(symbol, row, col,  0, +1, true);   // EAST
        flipped |= flipDirection(symbol, row, col, -1, -1, true);   // NW
        flipped |= flipDirection(symbol, row, col, -1, +1, true);   // NE
        flipped |= flipDirection(symbol, row, col, +1, -1, true);   // SW
        flipped |= flipDirection(symbol, row, col, +1, +1, true);   // SE

        if (flipped) board[row][col] = symbol;  // True - place the actual piece
        return flipped;  // True if ANY Of the directions were true.
    }

    /**
     * Get the various (valid) actions that are possible 
     * @param player - which player is moving
     * @return The Actions possible.
     **/
    public ArrayList<Action> getActions(int player) {
        ArrayList<Action> result = new ArrayList<Action>();
        char symbol = (player == 0) ? 'X' : 'O';
	
        // Go through EVERY spot on the board
        for (int r = 0; r < board.length; r++)
            for (int c = 0; c < board[r].length; c++)
                // Determine if this board location yields a valid move
                if (isValidMove(symbol, r, c))
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
    public boolean isValidMove(char symbol, int row, int col) {
        return
            board[row][col] == ' ' &&       // Space is open
            (flipDirection(symbol, row, col, -1,  0, false) ||   // NORTH
             flipDirection(symbol, row, col, +1,  0, false) ||   // SOUTH
             flipDirection(symbol, row, col,  0, -1, false) ||   // WEST
             flipDirection(symbol, row, col,  0, +1, false) ||   // EAST
             flipDirection(symbol, row, col, -1, -1, false) ||   // NW
             flipDirection(symbol, row, col, -1, +1, false) ||   // NE
             flipDirection(symbol, row, col, +1, -1, false) ||   // SW
             flipDirection(symbol, row, col, +1, +1, false));
    }

    /**
     * Try to flip the pieces in the given direction
     * @param flip True if it should FLIP, False if it should just SEE if it can flip
     **/
    private boolean flipDirection(char symbol, int row, int col, int dr, int dc, boolean flip) {
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

    /** 
     * Get Home and Away Scores
     **/
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }
    
    public synchronized void resign(int p) {
        done = true;
        winner = 1-p;
        computeScore(winner);
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
                System.out.println("Home won!"); break;
            case 'A':
                System.out.println("Away won!"); break;
            default:
                System.out.println("Unrecognized winner.");
            }
        }

        if (ai != null) ai.postWinner(result);  // Let AI know as well.
    }
}
