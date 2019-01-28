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

/***********************************************************
 * The Player represents a single individual bound to at most
 * one tournament.
 ***********************************************************/
public class Player {
    private int id;  // Useful if player needs to reconnect.
    private String name = null;
    private ClientConnection conn = null; 
    private int wins = 0;   // Wins for THIS tournament.
    private int losses = 0; // Losses for THIS tournament.
    private int ties = 0;   // Ties for THIS tournament.
    private static int nextID = 0;   // Next ID for player
    private RRTournament tour = null;  // Player belongs to just ONE tournament.
    private Match match = null;   // What match the player is currently assigned to...

    /**
     * Default Constructor
     **/
    public Player() {
        this.id = getNextID();
    }

    /*** Accessor and mutator methods ***/
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getID() { return id; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getTies() { return ties; }
    public int getTotalGames() { return wins + losses + ties; }
    public ClientConnection getConnection() { return conn; }
    synchronized public void setConnection(ClientConnection conn) { this.conn = conn; }
    public RRTournament getTour() { return tour; }
    synchronized public void setTour(RRTournament tour) { this.tour = tour; }

    // Assign a match to player but ONLY if it is currently not assigned.
    // Returns true if this assignment is successful and false if not (when player already has a match)
    synchronized public boolean setMatch(Match m) {
        if (match == null) { match = m; return true; } else return false;
    }
    public Match getMatch() { return match; }

    // Removes player from this current match...
    synchronized public void clearMatch() {
        match = null;
    }
    
    // Post a message (with new line) to the Client Connection associated with this Player
    // Returns true if sent and false if Connection non-existent.
    synchronized boolean postMessage(String message) {
        if (conn == null) return false;
        conn.postMessageLn(message);
        return true;
    }
    
    synchronized static private int getNextID() { return ++nextID; }
}
