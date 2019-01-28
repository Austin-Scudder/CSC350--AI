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

import java.util.ArrayList;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;

/***********************************************************
 * A basic round-robin tournament
 * system.  It facilitates creation of a tournament and the 
 * adding of players to tournaments.  Note that although every player
 * plays against every other player as both home and away, we do not
 * guarantee an ordering of the matches via rounds for example.
 * The reason is that a player (or student) might show up late or
 * a specific match might crash and we don't want to hold up the rest
 * of the tournament.  So, match selection will be done by availability
 * of the two opposing players.  Whenever a match can be played, it will
 * be played - as long as the tournament is active.
 ***********************************************************/
public class RRTournament implements Callable<Integer> {
    private GameFactory gameFactory;   // Used to create new games
    private ArrayList<Player> player;  // Players in this tournament
    private ArrayList<Match> match;    // List of Matches (to play)
    private boolean active;            // Is the tournament commencing.
    private boolean quit;              // If true, terminate the tournament completely
    private Player owner;              // The player that "owns" this tournament.
    private int id;                    // The id of this tournament
    private ExecutorService executor;    // Used to create new threads (for the various Matches)

    private static int nextID = 0;     // Next ID for Tournament
    
    public RRTournament(GameFactory g, Player owner) {
        player = new ArrayList<Player>();
        match = new ArrayList<Match>();
        active = false;
        quit = false;
        gameFactory = g;
        this.owner = owner;
        owner.setTour(this);
        this.id = getNextID();
        executor = Executors.newCachedThreadPool();
    }

    /**
     * Add the player to the tournament.
     * Create matches for this player against all other players currently in system.
     * This ensures a complete round-robin event.
     * @returns True if successfully added, false if not (e.g. player already present)
     **/
    public synchronized boolean addPlayer(Player p) {
        if (player.contains(p))
            return false;
	
        // Create the new matched pairs (other,p and p,other)
        for (Player other: player) {
            match.add(new Match(other, p, gameFactory.newGame()));
            match.add(new Match(p, other, gameFactory.newGame()));
        }

        p.setTour(this);  // Link this tournament to the player
        player.add(p);    // Add the player to the list
        return true;
    }
    
    /*** Accessor and mutator methods ***/
    public boolean isActive() { return active; }
    public synchronized void setActive(boolean active) { this.active = active; }
    public synchronized void terminate() {
        this.quit = true;

        // Inform all the players...
        for (Player p: player) p.postMessage("@TOUR:END");
    }
    
    public int getID() { return this.id; }
    public boolean isOwner(Player p) { return p == owner; }
    
    public Integer call() {
        while (!quit) {
            try {
                if (isActive()) startAvailableMatches();
                Thread.sleep(1000);  // So it doesn't run constantly...
            } catch (InterruptedException e) { }
        }

        return new Integer(0);
    }

    /**
     * Look through all matches and determine if any are ready to be played.
     * This happens when two players are both currently not playing in another match.
     * Start this match going.
     * If all matches are completed it also finished the tournament...
     **/
    private synchronized void startAvailableMatches() {
        for (Match m: match) {
            if (m.getState() == Match.State.NOT_STARTED) {
                // Check if home player is free...
                Player home = m.getHome();
                Player away = m.getAway();
                if (home.setMatch(m)) {
                    // Home is available... how about away?
                    if (away.setMatch(m)) {
                        // Away is also available... start'er up
                        debug("Starting match between " + home.getID() + " and " + away.getID());
                        FutureTask<Integer> task = new FutureTask<Integer>(m);
                        executor.execute(task);
                    } else {
                        home.clearMatch();  // Free the home player for another match
                    }
                }
            }
        }
    }

    /**
     * Return how many matches are NOT completed.
     **/
    public synchronized int matchesLeft() {
        int completed = 0;
        for (Match m: match) {
            if (m.getState() != Match.State.NOT_STARTED &&
                m.getState() != Match.State.IN_PROGRESS)
                completed++;
        }
        return match.size() - completed;
    }
    
    /**
     * Generate a report on this tournament.  See ServerProtocol text for format.
     * WARNING: Not done efficiently but number of matches should be small so not
     * critical to improve performance unnecessarily...
     **/
    synchronized String report() {
        String result = "";
        boolean first = true;
        for (Match m: match) {
            if (first) first = false; else result += ",";  // CSVs...
            Match.State state = m.getState();
            result += m.getHome().getID() + "/" + m.getAway().getID() + "/" +
                ((state == Match.State.NOT_STARTED) ? 'N' :
                 (state == Match.State.IN_PROGRESS) ? 'I' :
                 (state == Match.State.TIE) ? 'T' :
                 (state == Match.State.HOME_WIN) ? 'H' :
                 (state == Match.State.AWAY_WIN) ? 'A' : '?');
        }
        return result;
    }

    /**
     * Returns a "unique" header string - for debugging mainly.
     **/
    private String header() {
        return "Tour (" + getID() + "): ";
    }

    synchronized private void debug(String message) {
        System.out.println("DEBUG: " + header() + message);
    }

    synchronized static private int getNextID() { return ++nextID; }
}
