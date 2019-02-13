/*******************
 * Christian A. Duncan
 * CSC350: Intelligent Systems
 * Spring 2019
 *Names: Austin Scudder, Nicholas Molina, Matthew Jagiela
 * AI Game Client
 * This project is designed to link to a basic Game Server to test
 * AI-based solutions.
 * See README file for more details.
 ********************/
package cad.ai.game;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;



/***********************************************************
 * The AI system for a TicTacToeGame.
 *   Most of the game control is handled by the Server but
 *   the move selection is made here - either via user or an attached
 *   AI system.
 ***********************************************************/
public class TicTacToeAI2 extends AbstractAI {
	public TicTacToeGame game;  // The game that this AI system is playing
	protected Random ran;
	final static String filestate = "./Boards.txt";
	final static String filemoves = "./Moves.txt";
	// This gets the player and is 0 if you are home 1 if you are away
	int WLT = 3; // Will be 0 for loss 1 for win 
	Stack<String> boardstate = new Stack<String>();
	Stack <Integer> wins = new Stack<Integer>(); 
	int totalmoves = 0; 


	public TicTacToeAI2() {
		game = null;
		ran = new Random();
	}

	public synchronized void attachGame(Game g) {
		game = (TicTacToeGame) g;
	}

	/**
	 * Returns the Move as a String "S"
	 *    S=Slot chosen (0-8)
	 **/
	public synchronized String computeMove() {
		if (game == null) {
			System.err.println("CODE ERROR: AI is not attached to a game.");
			return "0";
		}

		char[] board = (char[]) game.getStateAsObject();

		// First see how many open slots there are
		int openSlots = 0;
		int i = 0;
		for (i = 0; i < board.length; i++)
			if (board[i] == ' ') openSlots++;

		// Now pick a random open slot
		int s = ran.nextInt(openSlots);

		// And get the proper row
		i = -1;
		while (s >= 0) {
			i++;
			if (board[i] == ' ') s--;  // One more open slot down
		}

		return "" + i;
	}	

	public int player(){
		int play = game.getPlayer();
		return play;

	}
	/**
	 * Inform AI who the winner is
	 *   result is either (H)ome win, (A)way win, (T)ie
	 **/
	@Override
	public synchronized void postWinner(char result) {
		//This decides the WLT
		int side = player();
		if (side == 0 & result == 'H'){
			WLT = 0; 
		}
		else if (side == 0 & result == 'A'){
			WLT = 1; 
		}
		else if (side == 1 & result == 'H' ) {
			WLT = 1;
		}
		else if (side == 1 & result == 'A') {
			WLT = 0; 
		}
		if( result == 'T') {
			WLT = 2;
		}
		wins.push(WLT);
		// This AI probably wants to store what it has learned
		// about this particular game.
		game = null;  // No longer playing a game though.
	}

	/**
	 * Shutdown the AI - allowing it to save its learned experience
	 **/

	@Override
	public synchronized void end() {
		try {

			BufferedWriter statewrite = new BufferedWriter(new FileWriter(filestate, true));
			while (boardstate.empty() && wins.empty()) {
				String relstate = (String) boardstate.pop();
				statewrite.write(relstate);
				statewrite.newLine();	
			}
			statewrite.close();

		} catch (FileNotFoundException e) {
			System.err.println("oops");
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		// This AI probably wants to store (in a file) what
		// it has learned from playing all the games so far...
	}
	public static class Record implements Serializable {
		static final long serialVersionUID = 1L;  // Used to verify it is same version of Record (in case it changes!)
		double alpha;  // The fitness score of this state

		public Record() { alpha = 1.0; }
	}


	public static void saveMap(HashMap<String, Record> map, String mapFileName) {
		ObjectOutputStream oos = null;
		try {
			// Open up the Object file for writing and write the HashMap
			oos = new ObjectOutputStream(new FileOutputStream(mapFileName));
			oos.writeObject(map);
			oos.close();
		} catch (IOException e) {
			System.out.println("Aborting!  Error saving map to file. " + e.getMessage());
			System.exit(1);
		}
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, Record> readMap(String mapFileName) {
		ObjectInputStream ois = null;
		try {
			// Open up the Object file for reading and read in the HashMap
			ois = new ObjectInputStream(new FileInputStream(filemoves));
			Object obj = ois.readObject();
			ois.close();
			return (HashMap<String,Record>) obj; // Typecast the Object read to a HashMap
		} catch (FileNotFoundException e) {
			System.out.println("Map file " + mapFileName + " was not found.  Using new map.");
			System.out.println("  Message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("Aborting!  Error processing file. " + e.getMessage());
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.out.println("Aborting!  Error processing file. Does not appear to save a Hash Map. " + e.getMessage());
			System.exit(1);
		}

		// Just return a blank map...
		return new HashMap<String,Record>();
	}

}


