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

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Iterator;

/***********************************************************
 * The GameServer represents the main server for the entire 
 * system.  It facilitates creation of a tournament and the 
 * adding of players to tournaments.
 ***********************************************************/
public class GameServer {
    public static final int DEFAULT_PORT = 1350;  // The default port to use...
    public static final int DEFAULT_MAX_CON = 30; // Default max number of connections for this server.
    public int maxConnections;                  // The maximum number of connections that the server will allow.
    public int port;                            // The port that this server listens on.
    private HashMap<Integer,Player> player;     // All the players connected to system
    private HashMap<Integer,RRTournament> tour; // Tournaments connected to system
    private ArrayList<ClientConnection> conn;   // All client connections (paired with players)
    private ServerSocket serverSocket;   // The socket that is bound to and listening for the server.
    private ExecutorService executor;    // Used to create new threads
    
    /**
     * Default Constructor.
     *    Creates a connection and player list.
     */
    public GameServer(int port, int mc) {
        this.port = port;
        this.maxConnections = mc;
        conn = new ArrayList<ClientConnection>();
        player = new HashMap<Integer,Player>();
        tour = new HashMap<Integer,RRTournament>();
        executor = Executors.newCachedThreadPool();
    }

    /**
     * Creates a new Round-Robin Tournament - of given type.
     **/
    public synchronized RRTournament createTournament(String type, Player owner) {
        // Determine what GameFactory to use
        GameFactory g = null;
        switch (type.toUpperCase()) {
        case "NIM": g = new NimFactory(); break;
        case "TTT": g = new TTTFactory(); break;
        case "OTHELLO_MICRO": g = new OthelloFactory(4); break;
        case "OTHELLO_MINI": g = new OthelloFactory(6); break;
        case "OTHELLO": g = new OthelloFactory(8); break;
        default: return null;  // No game recognized...
        }
        RRTournament rr = new RRTournament(g, owner);
        tour.put(rr.getID(), rr);

        // Start the Tournament Thread (BUT NOT THE TOURNAMENT)
        FutureTask<Integer> task = new FutureTask<Integer>(rr);
        executor.execute(task);
	
        return rr;
    }

    /**
     * Joins a tournament with the given ID... or if ID is negative
     * just joins any tournament.
     **/
    public synchronized int joinTournament(Player p, int id) {
        if (id < 0) {
            // Just get an arbitrary tournament (first in the hashmap listing)
            Iterator<RRTournament> it = tour.values().iterator();
            if (it.hasNext())
                return joinTournament(p, it.next());
            else
                return -1;  // No Tournaments in the map
        } else {
            RRTournament t = tour.get(new Integer(id));
            return  joinTournament(p, t);
        }
    }

    private synchronized int joinTournament(Player p, RRTournament tour) {
        if (tour == null) return -1;  // Tour was not provided (or found)
        tour.addPlayer(p);
        return tour.getID();
    }
    
    /**
     * Adds and registers a new player to the system.
     **/
    public synchronized Player registerPlayer() {
        Player p = new Player();
        player.put(p.getID(), p);
        return p;
    }
    
    /**
     * Get a returning player in the system.
     **/
    public synchronized Player getPlayer(int id) {
        return player.get(id);
    }
    
    /**
     * Register a new socket (client) connection.
     * @param sock 
     */
    private synchronized void addConnection(Socket sock) throws IOException {
        ClientConnection c = new ClientConnection(sock, this);
        if (conn.size() >= maxConnections)
            throw new IOException("Error: Maximum  number of clients (" + maxConnections + ") reached.");
	
        conn.add(c);  // Add connection to list of sockets
        FutureTask<Integer> task = new FutureTask<Integer>(c);
        executor.execute(task);
        // WARNING: Client Connections are never removed... even if they die.  TBD.
    }

    /**
     * This is the main body of the server.  It will start up a (server) socket to listen for connections.
     * When any connections arrive, it adds them to a client list and registers them into an active tournament.
     */
    private void run() {
        System.out.println("The AI Game Server: Hello.  Is there anybody out there?");
        try {
            // Create a server socket bound to the given port
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000);  // So accept does not block forever
            while (true) {
                try {
                    // Once a request is received, accept, and get the I/O streams.
                    Socket socket = serverSocket.accept();
                    addConnection(socket);   // Register and start the connection
                } catch (SocketTimeoutException e) {
                    // No connections yet... ok.
                    // Just keep listening forever
                } catch (IOException e) {
                    System.err.println("I/O Error: Terminating connection.");
                    System.err.println("  Message: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("ABORTING: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * The main body just parses any parameters passed and creates 
     * and runs a new Game Server with the proper initial settings.
     * @param args 
     */
    public static void main(String[] args) {
        // Defaults to use
        int port = DEFAULT_PORT;
        int mc = DEFAULT_MAX_CON;

        // Parse the arguments
        for (String arg: args) {
            try {
                String[] params = arg.split("=",2);
                switch (params[0]) {
                case "--help": printUsage(null); break;
                case "--port": port = Integer.parseInt(params[1]); break;
                case "--maxcon": mc = Integer.parseInt(params[1]); break;
                default:
                    printUsage("Unrecognized parameter: " + arg);
                }
            } catch (Exception e) {
                printUsage("Error processing parameter: " + arg);
            }
        }	    

        GameServer s = new GameServer(port, mc);
        s.run();
    }

    /**
     * Print Usage message and exit
     **/
    public static void printUsage(String message) {
        System.err.println("Usage: java cad.ai.server.GameServer [params]");
        System.err.println("       Where params are:");
        System.err.println("         --help           Print this help message.");
        System.err.println("         --port=integer   The port to listen on.");
        System.err.println("         --maxcon=integer The maximum number of connections to support.");
        if (message != null) System.err.println("       " + message);
        System.exit(1);
    }       
}
