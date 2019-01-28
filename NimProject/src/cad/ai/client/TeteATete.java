/*******************
 * Christian A. Duncan
 * CSC350: Intelligent Systems
 * Spring 2019
 *
 * AI Game Client
 * This project is designed to link to a basic Game Server to test
 * AI-based solutions.
* 
 * Tete-A-Tete
 *    This class allows for quick head-to-head network games
 * See README file for more details.
 ********************/
package cad.ai.client;

import java.net.*;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;
import cad.ai.game.*;

/***********************************************************
 * The TeteATete class is designed to allow two players to 
 * player against each other online.  The home player acts
 * as a "server" while the away is like the "client"
 ***********************************************************/
public class TeteATete {
    private final static int DEFAULT_PORT = 1350;   // The default port that this client connects to...
    private BufferedReader userIn = null;  // User connection
    private String hostname;
    private int player;
    private int port;
    private Socket sock = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private Game game = null;
    private Game serverGame = null;
    private AI ai = null;
    private static enum GameType { NIM, TTT };
    private GameType gameType;
    private int numGames;
    private int verbose;
    
    /**
     * Constructor
     * @param player Whether we are home (0) or away (1)
     * @param hostname The name of the machine to connect to.  If null, player is home.
     * @param port The port to connect to/listen on -1 means use default 1350
     * @param pname The name of the player
     * @param aiFlag Whether or not to create an AI for this player
     * @param gameType - what type of game to play.
     * @param numGames - the number of games to play.
     * @param verbose - how much to output [0 = quite, >0 = noisier]
     **/
    public TeteATete(int player, String hostname, int port, boolean aiFlag,
                     GameType gameType, int numGames, int verbose) {
        this.hostname = hostname;
        this.player = player;
        this.gameType = gameType;
        this.numGames = numGames;
        this.verbose = verbose;
        if (port >= 0) this.port = port; else this.port = DEFAULT_PORT;
        userIn = new BufferedReader(new InputStreamReader(System.in));
        game = null;
        if (aiFlag) createAI(); else this.ai = null;
    }

    /**
     * Create a new game to play.
     **/
    public synchronized void createNewGame() {
        switch (gameType) {
        case NIM:
            game = new NimGame(player, userIn, ai, false);  // This is a copy of the game to play
            if (hostname == null) 
                // Server also keeps track of the "true" game.
                serverGame = new NimGame(-1, userIn, null, true);
            break;
        case TTT:
            game = new TicTacToeGame(player, userIn, ai, false, verbose);  // This is a copy of the game to play
            if (hostname == null) 
                // Server also keeps track of the "true" game.
                serverGame = new TicTacToeGame(-1, userIn, null, true, verbose);
            break;
        }
    }

    /**
     * Create a new AI to play this type of game
     **/
    public synchronized void createAI() {
        switch (gameType) {
        case NIM:
            ai = new NimAI(); break;
        case TTT:
            ai = new TicTacToeAI(); break;
        }
    }
    
    /**
     * Start running the thread for this connection
     **/
    public void run() {
        try {
            if (hostname == null) {
                // We are the "Server" player.  Start listening for an opponent connection
                runServerSide();
            } else {
                // We are the "Client" player.  Connect to the host machine.
                runClientSide();
            }
        } catch (UnknownHostException e) {
            error("Unknown host: " + hostname);
            error("             " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            error("IO Error: Error establishing communication with server.");
            error("          " + e.getMessage());
            System.exit(1);
        }

        System.out.println("Good-bye!");
        close();
    }

    /**
     * Establish a connection from the server perspective...
     * Listening for a client to connect. And then play game from home perspective
     **/
    private void runServerSide() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Waiting for opponent to connect to this machine on port " + 
                           serverSocket.getLocalPort() + ".");
        this.sock = serverSocket.accept();
        this.out = new PrintWriter(this.sock.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));

        for (int i = 0; i < numGames; i++) {
            createNewGame();  // We are "home"

            while (!serverGame.isDone()) {
                String state = serverGame.getState(true);
                game.updateState(state);
                if (game.isPlayerTurn()) {
                    // Get the move based on current game state
                    String move = game.getMove();
                    processInput(move, player);
                } else {
                    // Transmit game state to away and wait for move response
                    out.println("@GAME:STATE:" + state); out.flush();
                    String move = in.readLine();
                    if (move == null) 
                        throw new IOException("Null value returned.  Assuming opponent is disconnected. Aborting.");
                    processInput(move, 1-player);
                }
            }
            int winner = serverGame.getWinner();
            char r = winner == 0 ? 'H' : winner == 1 ? 'A' : 'T';
            if (ai != null) ai.postWinner(r);  // Let our AI know the winner
            out.println("@GAME:RESULT:"+ r);
        }

        // Let our AI know it is completely done.
        if (ai != null) ai.end();
    }

    /**
     * Establish a connection from client perspective...
     * Connect to a server.  And then play game from away perspective
     **/
    private void runClientSide() throws UnknownHostException, IOException {
        this.sock = new Socket(hostname, port);
        this.out = new PrintWriter(this.sock.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));

        for (int i = 0; i < numGames; i++) {
            createNewGame();  // We are "away"

            while (game != null) { 
                if (game.isPlayerTurn()) {
                    // Get the move based on current game state
                    String move = game.getMove();
                    // Transmit the move
                    out.println(move); out.flush();
                } else {
                    // Wait for a message from the server...
                    String move = in.readLine();
                    if (move == null) 
                        throw new IOException("Null value returned.  Assuming opponent is disconnected. Aborting.");
                    processInput(move, 1-player);
                }
            }
        }

        // Let our AI know it is completely done.
        if (ai != null) ai.end();
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
            display(pieces[1]);
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
        debug("Error.  This should not need to be sent in TeteATete matches.");
    }
    
    synchronized private void processGameState(String[] pieces, int p) {
        if (pieces.length < 3)
            debug("No game state information was transmitted!");
        else if (serverGame != null)
            debug("The server does not receive game state changes...");
        else
            game.updateState(pieces[2]);
    }

    synchronized private void processGameMove(String[] pieces, int p) {
        if (pieces.length < 3)
            debug("No game move information was transmitted!");
        else if (serverGame == null)
            debug("The client does not process game moves directly.");
        else {
            String res = serverGame.processMove(p, pieces[2]);
            String message = res + (p == 0 ? "[Home]" : "[Away]");
            System.out.println(message);
            out.println("@GAME:" + message); out.flush();
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
            display(pieces[2]);
        }
    }
    
    synchronized private void processGameResult(String[] pieces, int p) {
        if (pieces.length < 3) {
            debug("Game Result was incorrectly transmitted.");
        } else {
            char result = pieces[2].charAt(0);
            game.postWinner(result);
            game = null;  // No longer need to store this game
        }
    }
    
    // Close the connection (can also be used to stop the thread)
    public synchronized void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (sock != null) sock.close();
        } catch (IOException e) {
            error("Error trying to close client connection: " + e.getMessage());
        } finally {
            in = null; out = null; sock = null;
        }
    }

    // For displaying debug, error, and regular messages
    private String header() { return "TeteATete: "; }
    private void error(String message) { System.err.println(header() + "ERROR: " + message); }
    private void debug(String message) { System.err.println(header() + "DEBUG: " + message); }
    private void display(String message) { System.out.println(header() + message); }

    /**
     * The main entry point.
     **/
    public static void main(String[] args) {
        // Defaults to use
        String hostname = null;
        int port = DEFAULT_PORT;
        int pid = -1;
        int tid = -1;
        boolean ai = true;
        int player = -1;
        GameType gameType = GameType.NIM;  // Default it NIM
        int repeat = 1;  // Number of games to play
        int verbose = 1; // How "noisy" to be

        // Parse the arguments
        for (String arg: args) {
            try {
                String[] params = arg.split("=",2);
                switch (params[0]) {
                case "--help": printUsage(null); break; // Help message only
                case "--home": player = 0; break;  // Home
                case "--away": player = 1;  break; // Away
                case "--host": hostname = params[1]; break;
                case "--port": port = Integer.parseInt(params[1]); break;
                case "--game":
                    switch (params[1].toUpperCase()) {
                    case "NIM": gameType = GameType.NIM; break;  // Silly sanity check...
                    case "TTT": gameType = GameType.TTT; break;
                    default: printUsage("Unrecognized game option: " + params[1]);
                    }
                    break;
                case "--repeat":
                    repeat = Integer.parseInt(params[1]); break;
                case "--verbose":
                    verbose = Integer.parseInt(params[1]); break;
                case "+ai": ai = true; break;
                case "-ai": ai = false; break;
                case "--ai":
                    if (params[1].equals("true")) ai=true;
                    else if (params[1].equals("false")) ai=false;
                    else printUsage("AI setting must be true or false");
                    break;
                default:
                    printUsage("Unrecognized parameter: " + arg);
                }
            } catch (Exception e) {
                printUsage("Error processing parameter: " + arg);
            }
        }	    

        if (player == -1) {
            // Mandatory parameter was not passed!
            printUsage("Neither Home nor Away was specified.");
        }
	
        TeteATete c = new TeteATete(player, hostname, port, ai, gameType,
                                    repeat, verbose);
        c.run();
    }

    /**
     * Print Usage message and exit
     **/
    public static void printUsage(String message) {
        System.err.println("Usage: java cad.ai.client.TeteATete [params]");
        System.err.println("       Where params are:");
        System.err.println("         --help      Print out this usage information.");
        System.err.println("         --home      Player is home.");
        System.err.println("         --away      Player is away.");
        System.err.println("                     One of away or home must be given.");
        System.err.println("         --game=XXX  Can be either NIM or TTT [default NIM]");
        System.err.println("         --repeat=X  Number of games to play [default 1]");
        System.err.println("         --verbose=X           -- 0=quiet, >0=Output more stuff.");
        System.err.println("         --host=hostname (if not given, acts as server)");
        System.err.println("         --port=hostport (integer)  Port to connect to or listen on");
        System.err.println("                                    DEFAULT is " + DEFAULT_PORT);
        System.err.println("         --ai=true/false [default true]");
        System.err.println("         [+/-]ai  Use or don't use AI");
        if (message != null) System.err.println("       " + message);
        System.exit(1);
    }       
}
