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

package cad.ai.server;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Callable;

/************************************************************
 * The ClientConnection represents a single connection to a client.
 * It primarily acts as the communication.  Passing information to client
 * and processing responses.
 ***********************************************************/
public class ClientConnection implements Callable<Integer> {
    private PrintWriter out = null;   // TO the client
    private BufferedReader in = null;  // FROM the client
    private Socket sock = null;        // The client socket itself
    private Player player = null;           // The player
    private Deque<String> messages;    // Messages to transmit to client
    private GameServer server = null;  // The game server
    
    ClientConnection(Socket sock, GameServer server) throws IOException {
        this.sock = sock;
        this.server = server;
        this.out = new PrintWriter(sock.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        this.messages = new ArrayDeque<String>();
    }

    /**
     * Returns a "unique" id for this client connection... for debugging mainly.
     **/
    private int getID() {
        return sock.getLocalPort();
    }

    /**
     * Returns a "unique" header string - for debugging mainly.
     **/
    private String header() {
        return "Client (" +
            sock.getInetAddress() + ":" + sock.getPort() + "): ";
    }

    synchronized private void debug(String message) {
        System.out.println("DEBUG: " + header() + message);
    }
    
    /**
     * Run the thread.
     *    This simply reports and processes client communication.
     **/
    public Integer call() {
        debug("Running...");
        while (in != null && out != null) {
            try {
                processInput();
                transmitMessages();
                Thread.sleep(100);  // Can't run super fast!
            }
            catch (InterruptedException e) { }
            catch (IOException e) {
                debug("I/O Exception: Ending connection. " + e.getMessage());
                close();
            }
        }
        return new Integer(0);  // All is well...
    }

    /**
     * Close the connection (can also be used to stop the thread)
     **/
    public synchronized void close() {
        try {
            if (player != null && player.getConnection() == this) player.setConnection(null);
            if (in != null) in.close();
            if (out != null) out.close();
            if (sock != null) sock.close();
        } catch (IOException e) {
            debug("Error trying to close client connection: " + e.getMessage());
        } finally {
            in = null; out = null; sock = null;
        }		
    }

    /**
     * Post a message to be transmitted to the Client (done next chance by Client Thread)
     **/
    public synchronized void postMessage(String message) {
        messages.addLast(message);   // Store the message in the messages Queue.
    }

    /**
     * Post a message to be transmitted to the Client (done next chance by Client Thread)
     * This also appends a new line to end of message.
     **/
    public synchronized void postMessageLn(String message) {
        messages.addLast(message + "\n");   // Store the message in the messages Queue.
    }

    /**
     * Transmit (all) messages in the Queue.
     **/
    private synchronized void transmitMessages() throws IOException {
        while (!messages.isEmpty()) {
            String m = messages.removeFirst();
            out.print(m);
        }
        out.flush();
    }
  
    /**
     * Get and process the input on the input stream
     **/
    private void processInput()	throws IOException {
        if (!in.ready()) return;  // No input ready to process
	
        // We'll process only ONE action per frame - the rest are just QUEUED
        // Of course, we could take all requests or just a few requests.
        // This is to prevent some BOT from generating LOTS of action requests.
        String message = in.readLine();

        if (message == null) {
            // End of transmission
            close();
        } else {
            // A message was provided.  Process this input message
            processInput(message);
        }
    }

    /**
     * Process the message provided.  Uses protocol described in ServerProtocol.txt
     **/
    synchronized private void processInput(String message) {
        try {
            String[] pieces = message.split(":", 5);
            String command = pieces[0].toUpperCase();
            switch (command) {
            case "@TOUR": processTournamentCommands(pieces); break;
            case "@NEW PLAYER": setNewPlayer(); break;
            case "@PLAYER": setPlayer(pieces); break;
            case "@NAME": setName(pieces); break;
            case "@GAME": processGameCommands(pieces); break;
            case "@GET NAME": getName(pieces); break;
            case "@PING": postMessageLn("@PONG"); break;
            case "@PONG": break; // Ignore it for now...
            default: postMessageLn("@ERROR:Unrecognized command.");
            }
        } catch (Exception e) {
            postMessageLn("@ERROR:Error processing command.");
            debug(e.getMessage());
        }
    }

    /**
     * Process the various tournament commands outlined in ServerProtocol.
     * Use pieces[1...]  -- skipping pieces[0] which is @TOUR
     **/
    private void processTournamentCommands(String[] pieces) {
        if (pieces.length < 2) {
            postMessageLn("@ERROR:A subcommand is required for TOUR.");
            return;
        }
        String command = pieces[1].toUpperCase();
        switch (command) {
        case "CREATE": createTournament(pieces); break;
        case "START": startTournament(); break;
        case "PAUSE": pauseTournament(); break;
        case "END": endTournament(); break;
        case "REPORT": reportTournament(); break;
        case "MATCHES LEFT": matchesLeftTournament(); break;
        case "JOIN":  joinTournament(pieces); break;
        default: postMessageLn("@ERROR:Unrecognized TOUR sub-command.");
        }
    }

    /**
     * Create a tournament with game type TYPE pieces[2] (passed as a string to GameServer).
     * ID # is returned back to sender - for people to join.
     **/
    private void createTournament(String[] pieces) {
        if (pieces.length < 3) {
            postMessageLn("@ERROR:A TYPE must be specified.");
            return;
        }
        if (player == null) {
            postMessageLn("@ERROR:No player set yet (to own the command).");
            return;
        }
        RRTournament rr = server.createTournament(pieces[2], player);
        if (rr == null) {
            postMessageLn("@ERROR:Type not recognized.");
        } else {
            postMessageLn("@TID:" + rr.getID());
        }
    }

    private void startTournament() {
        RRTournament tour = verifyTourOwner();
        if (tour != null) tour.setActive(true);
    }

    private void pauseTournament() {
        RRTournament tour = verifyTourOwner();
        if (tour != null) tour.setActive(false);
    }

    private void endTournament() {
        RRTournament tour = verifyTourOwner();
        if (tour != null) tour.terminate();
    }

    private void reportTournament() {
        RRTournament tour = verifyTourOwner();
        if (tour != null)
            postMessageLn("@REPORT:" + tour.report());
    }

    private void matchesLeftTournament() {
        RRTournament tour = verifyTourOwner();
        if (tour != null)
            postMessageLn("@MESSAGE: There are " + tour.matchesLeft() + " matches left to complete.");
    }

    /**
     * Helper function to get player's tournament (IF they are owner)
     *    Reports error if not available (and returns null)
     **/
    private RRTournament verifyTourOwner() {
        if (player == null) {
            postMessageLn("@ERROR:No player set yet (to own the command).");
            return null;
        }

        // Is this player the CREATOR of the Tournament?
        RRTournament tour = player.getTour();
        if (tour == null) {
            postMessageLn("@ERROR:Player is not attached to a tournament.");
            return null;
        } else if (!tour.isOwner(player)) {
            postMessageLn("@ERROR:Player is not the owner of the tournament.");
            return null;
        } else {
            return tour;
        }
    }
    
    /**
     * Join a tournament with given ID (pieces[1])
     **/
    private void joinTournament(String[] pieces) {
        if (player == null) {
            postMessageLn("@ERROR:No player set yet (to own the command).");
            return;
        }

        if (player.getTour() != null) {
            postMessageLn("@ERROR:Player already assigned to a tournament.");
            return;
        }
	
        // Join a tournament
        int id = -1;  // Negative IDs not allowed --- sentinel value.
        if (pieces.length > 2) {
            id = Integer.parseInt(pieces[1]);
        }
	
        int tid = server.joinTournament(player, id);
        if (tid >= 0)
            postMessageLn("@MESSAGE:Tournament " + tid + " successfully joined.");
        else
            postMessageLn("@ERROR:Failed to join a tournament.");
    }
    
    /**
     * Register a NEW player.
     **/
    private void setNewPlayer() {
        // This client is a new player
        if (player != null) {
            // Player already registered
            postMessageLn("@ERROR:Player already registered for this account.");
        } else {
            player = server.registerPlayer();
            player.setConnection(this);  // So, Player can communicate with Client
            postMessageLn("@PID:"+player.getID());
        }
    }
    
    /**
     * Set player using given ID (pieces[1]).
     **/
    private void setPlayer(String[] pieces) {
        // This client is a returning player
        if (pieces.length < 2) {
            postMessageLn("@ERROR:ID.  An ID is required for PLAYER.");
            return;
        }
        int id = Integer.parseInt(pieces[1]);
        Player p = server.getPlayer(id);
        if (p == null) {
            postMessageLn("@ERROR:ID was not recognized.");
        } else if (p.getConnection() != null) {
            postMessageLn("@ERROR:Player already connected to another client.");
        } else {
            player = p;
            player.setConnection(this);   // So, Player can communicate with Client
            postMessageLn("@PID:"+player.getID());
        }
    }
    
    /**
     * Set the Player's name to pieces[1]
     **/
    private void setName(String[] pieces) {
        if (player == null) {
            postMessageLn("@ERROR:No player set yet.");
        } else if (pieces.length < 2) {
            postMessageLn("@ERROR:A NAME is required.");
        } else {
            player.setName(pieces[1]);
        }
    }

    /**
     * Get the Player's name with given ID pieces[1]
     **/
    private void getName(String[] pieces) {
        if (pieces.length < 2) {
            postMessageLn("@ERROR:An ID is required.");
        } else {
            try {
                int id = Integer.parseInt(pieces[1]);
                Player p = server.getPlayer(id);
                if (p == null) {
                    postMessageLn("@ERROR:ID was not recognized.");
                } else {
                    postMessageLn("@NAME:" + p.getName() + ":" + id);
                }
            } catch (NumberFormatException e) {
                postMessageLn("@ERROR:Error parsing ID: " + pieces[1]);
            }
        }
    }
    
    
    /**
     * Send a game "move" message to player's current match...
     **/
    private void processGameCommands(String[] pieces) {
        if (pieces.length < 2) {
            postMessageLn("@ERROR:A subcommand is required for GAME.");
            return;
        }
        if (player == null) {
            postMessageLn("@ERROR:No player set yet.");
            return;
        }
        Match m = player.getMatch();
        if (m == null) {
            postMessageLn("@ERROR:Player is not playing in a match currently.");
            return;
        }

        String command = pieces[1].toUpperCase();
        switch (command) {
        case "MOVE": m.postMessage(player, pieces[2]); break;
        default: postMessageLn("@ERROR:Unrecognized GAME sub-command.");
        }
    }
}
