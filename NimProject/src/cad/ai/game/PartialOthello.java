/*******************
 * Christian A. Duncan
 * CSC350: Intelligent Systems
 * Spring 2019
 *
 * AI Game Interface
 * This project is designed to support a simple direct interaction of a 2-player turn-based game.
 * See README file for more details.
 ********************/

package cad.ai.game;

import java.net.*;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;
import cad.ai.game.*;

/***********************************************************
 * The PartialOthello class plays part of an othello game automatically...
 * and then has the AI play the rest.  This is just to see the moves made
 * by the AI for this game.
 ***********************************************************/
public class PartialOthello {
    private BufferedReader userIn = null;  // Access to user input
    private Game game[]; 
    private Game serverGame = null;
    private AI ai[];
    private int verbose;
    
    /**
     * Constructor
     * @param verbose - how much to output [0 = quite, >0 = noisier]
     **/
    public PartialOthello(int verbose) {
        game = new Game[2];  // A copy of the game for each player.
        ai = new AI[2];      // A spot for ai for each player.
        this.verbose = verbose;

        ai[0] = new OthelloAI();
        ai[1] = new OthelloAI();
    }

    /**
     * Start playing the game
     **/
    public void run() {
        createGame();
        playFixedGame();  // Play a few rounds of the game first...
        playGame();       // Play the rest of the game via AI

        // Let both AI's know we are done - so it can save state...
        for (int i = 0; i < ai.length; i++)  
            if (ai[i] != null) ai[i].end();
	    
        System.out.println("Good-bye!");
    }

    /**
     * Create a new "game" based on Game Type
     **/
    public void createGame() {
        for (int p = 0; p < 2; p++)
            game[p] = new OthelloGame(p, null, ai[p], false, verbose, 6);
        serverGame = new OthelloGame(-1, null, null, true, verbose, 6);
    }

    /**
     * Play a few turns of the game - to reduce the search space!
     **/
    private void playFixedGame() {
        serverGame.processMove(0, "1c");
        serverGame.processMove(1, "1d");
        serverGame.processMove(0, "1e");
        serverGame.processMove(1, "3b");
        serverGame.processMove(0, "4c");
        serverGame.processMove(1, "0d");
        serverGame.processMove(0, "2e");
        serverGame.processMove(1, "3e");
        serverGame.processMove(0, "4b");
        serverGame.processMove(1, "3f");
        serverGame.processMove(0, "4d");
        serverGame.processMove(1, "2b");
        serverGame.processMove(0, "3a");
        serverGame.processMove(1, "0f");
        serverGame.processMove(0, "0e");
        serverGame.processMove(1, "5a");
    }

    /**
     * Play the current game
     **/
    private void playGame() {
        while (!serverGame.isDone()) {
            String state = serverGame.getState(true);  // Get the state
            int turn = serverGame.getTurn();  // Whose turn is it
            if (turn >= 0 && turn <= 1) {
                // Get the move based on current game state
                game[turn].updateState(state);
                String move = game[turn].getMove();
                processInput(move, turn);
            }
        }
	
        // Display the board one last time
        if (verbose > 0) serverGame.displayState();
	
        // Let the games both know the winner...
        int winner = serverGame.getWinner();
        if (verbose > 0) {
            if (winner == 0) {
                System.out.println("Home won.");
            } else if (winner == 1) {
                System.out.println("Away won.");
            } else {
                System.out.println("It was a tie.");  // Not possible in NIM.
            }
        }
	
        char r = winner == 0 ? 'H' : winner == 1 ? 'A' : 'T';
        game[0].postWinner(r);
        game[1].postWinner(r);
    }

    /**
     * Process the message provided.  Uses protocol described in ServerProtocol.txt
     * @param message  The message to process
     * @param p The player that sent it
     **/
    synchronized private void processInput(String message, int p) {
        try {
            String[] pieces = message.split(":", 5);
            String command = pieces[0].toUpperCase();
            switch (command) {
            case "@ERROR": processErrorMessage(pieces, p); break;
            case "@MESSAGE": processMessage(pieces, p); break;
            case "@GAME": processGameCommands(pieces, p); break;
            default: error("Unrecognized command from server. " + message);
            }
        } catch (Exception e) {
            error("Error processing command (" + message + "). " + e.getMessage());
        }
    }

    synchronized private void processErrorMessage(String[] pieces, int p) {
        if (pieces.length < 2) {
            debug("Error Message was incorrectly transmitted.");
        } else {
            display("ERROR: " + pieces[1]);
        }
    }

    synchronized private void processMessage(String[] pieces, int p) {
        if (pieces.length < 2) {
            debug("Message was incorrectly transmitted.");
        } else {
            if (verbose > 0) display(pieces[1]);
        }
    }
	
    synchronized private void processGameCommands(String[] pieces, int p) {
        if (pieces.length < 2) {
            debug("Error.  No game subcommand submitted...");
            return;
        }
        String command = pieces[1];
        switch(command) {
        case "START": processGameStart(pieces, p); break;
        case "STATE": processGameState(pieces, p); break;
        case "MOVE": processGameMove(pieces, p); break;
        case "ERROR": processGameErrorMessage(pieces, p); break;
        case "MESSAGE": processGameMessage(pieces, p); break;
        case "RESULT": processGameResult(pieces, p); break;
        default: debug("Unrecognized game command transmitted: " + command);
        }
    }

    synchronized private void processGameStart(String[] pieces, int p) {
        debug("Error.  This should not need to be sent in PlayGame matches.");
    }
    
    synchronized private void processGameState(String[] pieces, int p) {
        debug("Hmm, this should not be transmitted as input to process.  Ignoring...");
    }

    synchronized private void processGameMove(String[] pieces, int p) {
        if (pieces.length < 3)
            debug("No game move information was transmitted!");
        else {
            System.out.println("SELECTED MOVE: " + pieces[2]);
            String res = serverGame.processMove(p, pieces[2]);
            if (verbose > 0) {
                String message = res + (p == 0 ? "[Home]" : "[Away]");
                System.out.println(message);
            }
        }
    }

    synchronized private void processGameErrorMessage(String[] pieces, int p) {
        if (pieces.length < 3) {
            debug("Game Error Message was incorrectly transmitted.");
        } else {
            display("GAME ERROR: " + pieces[2]);
        }
    }
    
    synchronized private void processGameMessage(String[] pieces, int p) {
        if (pieces.length < 3) {
            debug("Game Message was incorrectly transmitted.");
        } else {
            if (verbose > 0) display(pieces[2]);
        }
    }
    
    synchronized private void processGameResult(String[] pieces, int p) {
        debug("Hmm, this should not be transmitted either.");
    }
    
    // For displaying debug, error, and regular messages
    private void error(String message) { System.err.println("ERROR: " + message); }
    private void debug(String message) { System.err.println("DEBUG: " + message); }
    private void display(String message) { System.out.println(message); }

    /**
     * The main entry point.
     **/
    public static void main(String[] args) {
        // Defaults to use
        int verbose = 1; // How "noisy" to be
	
        // Parse the arguments
        for (String arg: args) {
            try {
                String[] params = arg.split("=",2);
                switch (params[0]) {
                case "--help":
                    printUsage(null);  // just print the Help message and exit
                    break;
                case "--verbose":
                    verbose = Integer.parseInt(params[1]); break;
                default:
                    printUsage("Unrecognized parameter: " + arg);
                }
            } catch (Exception e) {
                printUsage("Error processing parameter: " + arg);
            }
        }

        PartialOthello c = new PartialOthello(verbose);
        c.run();
    }

    /**
     * Print Usage message and exit
     **/
    public static void printUsage(String message) {
        System.err.println("Usage: java cad.ai.game.PartialOthello [params]");
        System.err.println("       Where params are:");
        System.err.println("         --help                -- Print this usage message");
        System.err.println("         --verbose=X           -- 0=quiet, >0=Output more stuff.");
        if (message != null) 
            System.err.println("       " + message);
        System.exit(1);
    }       
}
