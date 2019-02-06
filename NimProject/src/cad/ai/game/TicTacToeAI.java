/*******************
 * Christian A. Duncan
 * CSC350: Intelligent Systems
 * Spring 2019
 *Austin Scudder, Matt Jagiela, Nicholas Molina 
 * AI Game Client
 * This project is designed to link to a basic Game Server to test
 * AI-based solutions.
 * See README file for more details.
 ********************/
package cad.ai.game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

/***********************************************************
 * The AI system for a TicTacToeGame.
 *   Most of the game control is handled by the Server but
 *   the move selection is made here - either via user or an attached
 *   AI system.
 ***********************************************************/
public class TicTacToeAI extends AbstractAI {
	public TicTacToeGame game;  // The game that this AI system is playing
	protected Random ran;
	HashMap<String, Array[]> record = new HashMap<String, Array[]>(); // Keeps the current board state choices based on recorded choices
	Stack moves = new Stack ();
	//We need the board state to be stored in a hashmap and the chosen move
	//Take the board state in as a String
	//maybe store the full record rather than the probabilities.
	public TicTacToeAI() {
		game = null;
		ran = new Random();
	}

	public synchronized void attachGame(Game g) {
		game = (TicTacToeGame) g;
	}

	/**
	 * Returns the Move as a String "S"
	 *    S=Slot chosen (0-8)
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
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
		String temp = Integer.toString(i);
		moves.push(temp);
		return "" + i;
	}	

	/**
	 * Inform AI who the winner is
	 *   result is either (H)ome win, (A)way win, (T)ie
	 **/

	@Override
	public synchronized void postWinner(char result){
		///change the file path for your directory. 
		final String filename = "./Test-TTTBrain.txt";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));	
			writer.newLine();
			while (!moves.empty()) {
				String move = (String) moves.pop();
				writer.write(move);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			System.err.println("oops");
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// This AI probably wants to store what it has learned
		// about this particular game.
		game = null;  // No longer playing a game though.
	}


	/**
	 * Shutdown the AI - allowing it to save its learned experience
	 **/

	@Override
	public synchronized void end(){
		Record.read2();
		Record.printarray();
		// This AI probably wants to store (in a file) what
		// it has learned from playing all the games so far...
	}

	public static class Record{
		int[] wins; 
		int[] loss; 
		int[] tie; 
		static String[] words = new String[10];
		static int total;
		static String temp; 
		static int t =1; 
		static String filename = "./Test-TTTBrain.txt";

		public static void read2() {
			try {
				FileInputStream in = new FileInputStream(filename);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));

				String strLine;
				strLine = br.readLine();
				//Read File Line By Line
				while (strLine = br.readLine() != null )  {
				  // Print the content on the console
					
				  System.out.println (strLine);
				  strLine = br.readLine()
				}

				//Close the input stream
				in.close();
			} catch (FileNotFoundException e) {
				System.err.println("file Not found check link 139 and 135");
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


/*
		public static void read(){
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(filename));

				while ((t < 10) ) { 
					temp = in.readLine(); 

					System.out.println( "iterator= " + t + "data: " + temp);
					words[total] = temp;
					++total; 
					++t;
					if (in.readLine().isEmpty()) {
						t = 0; 
					}
				}
				temp = in.readLine();

				in.close();
			} catch (FileNotFoundException e) {
				System.err.println("oops");
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	*/
		public static void printarray() {
			int j = 0; 
			System.out.println(" ");
			while (j<words.length) {
				System.out.print(words[j]);
			}
		}


	}
}





