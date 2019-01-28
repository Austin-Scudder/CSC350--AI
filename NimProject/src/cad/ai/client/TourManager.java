/*******************
 * Christian A. Duncan
 * CSC350: Intelligent Systems
 * Spring 2019
 *
 * AI Game Client
 * This project is designed to link to a basic Game Server to test
 * AI-based solutions.
 *
 * TourManager:
 *    This class is designed to manager tournaments (in a text format)
 * See README file for more details.
 ********************/
package cad.ai.client;

import java.net.*;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

/***********************************************************
 * The TourManager represents a class that helps start and manage
 * tournaments..  It is basically menu driven.
 ***********************************************************/
public class TourManager {
    // A record for a given player
    private class Record {
        private Integer id;    // The player id
        private String name;   // The player's Name
        private int win;   // Number of wins for this player
        private int loss;  // Number of losses for this player
        private int tie;   // Number of ties for this player
        private int score; // The score for this player (W = 3 pts, T = 1 pt)
        private boolean scheduled;  // Is this player scheduled for ANY matches at all?

        public Record(Integer _id) {
            id = _id; win = loss = tie = score = 0;
            scheduled = false;
            name = null;
        }

        // Record the result of a given match
        public synchronized void recordMatch(boolean home,
                                             Integer opp, char res) {
            scheduled = true;  // It is either playing or has played a match...
            switch (res) {
            case 'H': if (home) addWin(); else addLoss(); break;
            case 'A': if (home) addLoss(); else addWin(); break;
            case 'T': addTie(); break;
            default: // can just ignore other matches
            }
        }
	    
        private void addWin() { win++; score += 3; }
        private void addLoss() { loss++; }
        private void addTie() { tie++; score += 1;}
        public synchronized void reset() { win = loss = tie = score = 0; scheduled = false; }
        public synchronized void setName(String n) { name = n; }
        public synchronized boolean isScheduled() { return scheduled; }
        public String toString() {
            String result = "" + id;
            if (name != null) result += "[" + name + "]";
            else if (conn != null) {
                // Request the name - for next time anyway
                conn.postMessageLn("@GET NAME:" + id);
            }
            result += ": " + score + " (" + win + "-" + loss + "-" + tie + ")";
            return result;
        }
    }
    
    private BufferedReader userIn = null;  // User connection
    private String hostname;
    private int port = 1350;   // The default port that this client connects to...
    private String pname;      // The name of the player
    private int pid;           // The player's id on the Server
    private int tid;           // The tournament's id (generally not useful if only one Tour running)
    private ExecutorService executor;    // Used to create new threads
    private Connection conn = null;
    HashMap<Integer, Record> record; // The record associated with each player
    char table[][] = null;   // A huge table to display scores... (INEFFICIENT HACK)
    private boolean done = false;
    
    /**
     * Constructor
     * @param hostname The name of the machine to connect to
     * @param port The port to connect to -1 means use default 1350
     * @param pname The name of the tournament organizer
     * @param pid The id of the player if returning or -1 if a new player
     *
     * In general, the port and pid can just be -1.
     * The exception is if the player's TourManager crashed or lost connection and had to reconnect.
     * The pid can be used to connect back to the same player on the server.
     **/
    public TourManager(String hostname, int port, String pname, int pid) {
        this.hostname = hostname;
        if (port >= 0) this.port = port;
        this.pname = pname;
        this.pid = pid;
        this.record = new HashMap<Integer, Record>();
        this.table = new char[20][20];
        userIn = new BufferedReader(new InputStreamReader(System.in));
        executor = Executors.newCachedThreadPool();
        done = false;
    }
    
    public synchronized void setDone(boolean flag) { done = flag; }
    
    /**
     * Start running the thread for this connection
     **/
    public void run() {
        System.out.println("Game Tournament Interactive System");
        System.out.println("Enter ? for the menu options.");
        try {
            startConnection();
            // Establish connection with the Game Server
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + hostname);
            System.err.println("             " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("IO Error: Error establishing communication with server.");
            System.err.println("          " + e.getMessage());
            System.exit(1);
        }

        while (!done) {
            try {
                processUserInput();
                Thread.sleep(100);  // Sleep for a bit
            } catch (InterruptedException e) {
            } catch (IOException e) {
                System.err.println("Error reading user input.");
            }
        }

    	System.out.println("Good-bye!");
        conn.close();
        executor.shutdown();
    }
    
    /***
     * Process any user input that might have been entered.
     * This just send it over to the server really
     ***/
    private void processUserInput() throws IOException {
        if (conn == null) return;   // No connection to server... yet?
        if (!userIn.ready()) return;  // Nothing ready to process
        String selection = userIn.readLine();  // Get the line...
        if (selection == null) {
            // Connection is Over
            conn.close();
            conn = null;
            return;
        } else if (selection.equals("?")) {
            printMenu();
        } else {
            try {
                int opt = Integer.parseInt(selection);
                switch (opt) {
                case 0: processRawCommand(); break;
                case 1: processCreateTour(); break;
                case 2: conn.postMessageLn("@TOUR:START"); break;
                case 3: conn.postMessageLn("@TOUR:PAUSE"); break;
                case 4: conn.postMessageLn("@TOUR:END"); break;
                case 5: conn.postMessageLn("@TOUR:REPORT"); break;
                case 6: System.out.println("Good-bye?"); System.exit(0); break;
                default: System.err.println("Unrecognized option.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Menu option should be a number.");
            }
        }
    }

    private void processRawCommand() throws IOException {
        System.out.println("Please enter a raw command to send to game server.");
        String command = userIn.readLine();
        conn.postMessageLn(command);
    }

    private void processCreateTour() throws IOException {
        System.out.println("Please enter name of game for the tournament.");
        String game = userIn.readLine();
        conn.postMessageLn("@TOUR:CREATE:"+game);
    }

    
    /***
     * Print out the menu system.
     ***/
    private void printMenu() {
        System.out.println("0. Enter command string directly.");
        System.out.println("1. Create tournament.");
        System.out.println("2. Start tournament.");
        System.out.println("3. Pause tournament.");
        System.out.println("4. End tournament.");
        System.out.println("5. Print report of tournament status.");
        System.out.println("6. Quit.");
    }

    /**
     * Get a player index from player ID.
     *    This is done because players might not be labelled 1...N!
     **/
    synchronized private Record getRecord(Integer id, boolean create) {
        Record res = record.get(id);
        if (res == null && create) {
            // Creae a new one
            res = new Record(id);
            record.put(id, res);
        }
        return res;
    }

    /**
     * Reset all the records in the table
     **/
    synchronized private void resetRecords() {
        for (Record r: record.values()) r.reset();
        for (int r = 0; r < table.length; r++)
            for (int c = 0; c < table[r].length; c++)
                table[r][c] = '-';
    }

    /**
     * Print out the records
     **/
    synchronized private void printRecords() {
        Record rec = null;
        // Print column header
        System.out.print("   ");
        for (int c = 0; c < table[0].length; c++) {
            rec = getRecord(c, false);
            if (rec != null && rec.isScheduled()) {
                if (c > 9)
                    System.out.print(c/10 + " ");
                else
                    System.out.print("  ");
            }
        }
        System.out.println();

        System.out.print("   ");
        for (int c = 0; c < table[0].length; c++) {
            rec = getRecord(c, false);
            if (rec != null && rec.isScheduled()) {
                System.out.print(c%10 + " ");
            }
        }
        System.out.println();

        for (int r = 0; r < table.length; r++) {
            rec = getRecord(r, false);
            if (rec != null && rec.isScheduled()) {
                System.out.format("%2d ", r);
                for (int c = 0; c < table[r].length; c++) {
                    rec = getRecord(c, false);
                    if (rec != null && rec.isScheduled())
                        System.out.print(table[r][c] + " ");
                }
                System.out.println();
            }
        }

        System.out.println("========== Rankings ==========");
        for (Record r: record.values()) System.out.println(r);
        System.out.println("==============================");
    }
    
    private class Connection implements Callable<Integer> {
        private Socket sock = null;
        private PrintWriter out = null;
        private BufferedReader in = null;
        private Deque<String> messages = null;
	
        public Connection() throws UnknownHostException, IOException {
            this.sock = new Socket(hostname, port);
            this.out = new PrintWriter(sock.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            this.messages = new ArrayDeque<String>();
            initialHandshake();
        }
	
        /***
         * The initial connection to the game server based on the parameters specified
         * at creation time.
         ***/
        private void initialHandshake() {
            try {
                String response = null;  // The response to a query
                String pieces[] = null;  // The response in pieces
                // Create player
                if (pid == -1) {
                    out.println("@NEW PLAYER");    // New player
                } else {
                    out.println("@PLAYER:" + pid); // Returning player
                }

                // Set player name
                out.println("@NAME:" + pname);
            } catch (Exception e) {
                error("ABORTING: Communication error during handshake. " + e.getMessage());
                System.exit(1);
            }
        }

        public Integer call() {
            while (in != null && out != null) {
                try {
                    checkActive();
                    processInput();
                    transmitMessages();
                    Thread.sleep(10);  // Can't run super fast!
                }
                catch (InterruptedException e) { }
                catch (IOException e) {
                    error("I/O Exception: Ending connection.  " + e.getMessage());
                    this.close();
                }
            }
            return new Integer(0);
        }			    

        /**
         * Determine if communication is still active
         **/
        private long lastReceived = 0;
        private long lastPing = 0;
        private int pingCount = 0;
        private static final int MAX_PING = 10;
        private static final long MAX_QUIET = 200;
        private synchronized void checkActive() {
            long time = System.currentTimeMillis();
            if (time - lastReceived < MAX_QUIET) {
                // Nothing to do, we have received a message from server recently
                pingCount = 0;
                return;
            }

            if (time - lastPing > MAX_QUIET) {
                // We haven't transmitted a ping in a while
                if (pingCount < MAX_PING) {
                    // Try pinging the server again
                    postMessageLn("@PING");
                    lastPing = time;
                    pingCount++;
                } else {
                    // It has been far too long...
                    setDone(true);
                    this.close();
                }
            }
        }

        /**
         * Post a message to be transmitted to the Server (done next chance by Connection Thread)
         **/
        public synchronized void postMessage(String message) {
            messages.addLast(message);   // Store the message in the messages Queue.
        }
	
        /**
         * Post a message to be transmitted to the Server (done next chance by Connection Thread)
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
            long time = System.currentTimeMillis();
            String message = in.readLine();
            lastReceived = time;  // For keeping connection alive...
	    
            if (message == null) {
                // End of transmission
                this.close();
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
                case "@ERROR": processErrorMessage(pieces); break;
                case "@MESSAGE": processMessage(pieces); break;
                case "@PID": processPID(pieces); break;
                case "@TID": processTID(pieces); break;
                case "@REPORT": processReport(pieces); break;
                case "@NAME": processName(pieces); break;
                case "@GAME": processGameCommands(pieces); break;
                case "@PING": postMessageLn("@PONG"); break;
                case "@PONG": break;  // Ignore (already registered message received)
                default: error("Unrecognized command from server. " + message);
                }
            } catch (Exception e) {
                error("Error processing command (" + message + "). " + e.getMessage());
            }
        }

        /**
         * Process a Name that has been sent (in response to name request)
         **/
        synchronized private void processName(String[] pieces) {
            try {
                String name = pieces[1];
                Integer pid = new Integer(pieces[2]);
                Record rec = record.get(pid);  // Get the record
                if (rec != null) 
                    rec.setName(name);
            } catch (Exception e) {
                debug("Name response was transmitted incorrectly.");
            }
        }
	
        /**
         * Display the scores between all the matches
         * as well as computing the tallies of win/loss/ties per player
         **/
        synchronized private void processReport(String[] pieces) {
            if (pieces.length < 2) {
                debug("Report was transmitted without accompanying data.");
            } else {
                System.out.println("Report received: " + pieces[1]);
                String[] match = pieces[1].split(",");

                resetRecords();   // Reset before updating scores
		
                for (String m: match) {
                    // We need to split this string up too HID/AID/STATE
                    String[] val = m.split("/");

                    // Get (or Create) the Record associated with this player
                    Integer hid = new Integer(val[0]);
                    Integer aid = new Integer(val[1]);
                    char res = val[2].charAt(0);
                    Record home = getRecord(hid, true);
                    Record away = getRecord(aid, true);
                    home.recordMatch(true, aid, res);
                    away.recordMatch(false, hid, res);
                    table[hid][aid] = res;
                }

                printRecords();
            }
        }

        synchronized private void processPID(String[] pieces) {
            if (pieces.length < 2) {
                debug("PID was transmitted without a valid ID.");
            } else {
                try { 
                    pid = Integer.parseInt(pieces[1]);
                    System.out.println("Player registered with ID=" + pid +
                                       ".  Remember this in case you have to reconnect.");
                } catch (Exception e) {
                    debug("PID was not properly transmitted as an integer: " + pieces[1]);
                }
            }
        }

        synchronized private void processTID(String[] pieces) {
            if (pieces.length < 2) {
                debug("TID was transmitted without a valid ID.");
            } else {
                try { 
                    tid = Integer.parseInt(pieces[1]);
                    System.out.println("Tournament registered with ID=" + tid +
                                       ".  This is not needed unless the server has more than one tournament running.");
                } catch (Exception e) {
                    debug("TID was not properly transmitted as an integer: " + pieces[1]);
                }
            }
        }

        synchronized private void processErrorMessage(String[] pieces) {
            if (pieces.length < 2) {
                debug("Error Message was incorrectly transmitted by server.");
            } else {
                display("ERROR: " + pieces[1]);
            }
        }

        synchronized private void processMessage(String[] pieces) {
            if (pieces.length < 2) {
                debug("Message was incorrectly transmitted by server.");
            } else {
                display(pieces[1]);
            }
        }

        synchronized private void processGameCommands(String[] pieces) {
            debug("Game command sent.  Should not be sent to a tournament organizer.");
            debug("Received: " + pieces[1]);
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
                setDone(true);  // Connection done, nothing left to do...
            }
        }

        // For displaying debug, error, and regular messages
        private String header() { return "TourManager: "; }
        private void error(String message) { System.err.println(header() + message); }
        private void debug(String message) { System.err.println(header() + "DEBUG: " + message); }
        private void display(String message) {
            System.out.println("Server: " + message);
        }
    }
    
    /***
     * Start a basic connection running that reads game state updates
     * from the Game Server
     ***/
    private void startConnection() throws UnknownHostException, IOException {
        conn = new Connection();
        FutureTask<Integer> task = new FutureTask<Integer>(conn);
        executor.execute(task);
    }

    /**
     * The main entry point.
     **/
    public static void main(String[] args) {
        // Defaults to use
        String hostname = "localhost";
        int port = -1;
        String name = "???";
        int pid = -1;

        // Parse the arguments
        for (String arg: args) {
            try {
                String[] params = arg.split("=",2);
                switch (params[0]) {
                case "--help": printUsage(null); break;
                case "--host": hostname = params[1]; break;
                case "--port": port = Integer.parseInt(params[1]); break;
                case "--name": name = params[1]; break;
                case "--pid": pid = Integer.parseInt(params[1]); break;
                default:
                    printUsage("Unrecognized parameter: " + arg);
                }
            } catch (Exception e) {
                printUsage("Error processing parameter: " + arg);
            }
        }	    

        TourManager c = new TourManager(hostname, port, name, pid);
        c.run();
    }

    /**
     * Print Usage message and exit
     **/
    public static void printUsage(String message) {
        System.err.println("Usage: java cad.ai.client.TourManager [params]");
        System.err.println("       Where params are:");
        System.err.println("         --help   Print this help message.");
        System.err.println("         --host=hostname");
        System.err.println("         --port=integer The port to connect to");
        System.err.println("         --name=playerName");
        System.err.println("         --pid=playerID");
        if (message != null) System.err.println("       " + message);
        System.exit(1);
    }       
}
