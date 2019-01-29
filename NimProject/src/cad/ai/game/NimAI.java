/*******************
 * Christian A. Duncan
 * Modified by: Austin Scudder, Nicholas Molina, Matthew Jagiela
 *
 * CSC350: Intelligent Systems
 * Spring 2019
 *
 * AI Game Client
 * This project is designed to link to a basic Game Server to test
 * AI-based solutions.
 *
 * NimAI:
 *    This class is the main AI system for the NIM game.
 *
 * See README file for more details.
 ********************/
package cad.ai.game;

import java.util.Random;

/***********************************************************
 * The AI system for a NimGame.
 *   Most of the game control is handled by the Server but
 *   the move selection is made here - either via user or an attached
 *   AI system.
 ***********************************************************/
public class NimAI extends AbstractAI {
	protected NimGame game;  // The game that this AI system is playing
	protected Random ran;

	public NimAI() {
		game = null;
		ran = new Random();
	}

	public void attachGame(Game g) {
		game = (NimGame) g;
	}

	/**
	 * Returns the Move as a String "R,S"
	 *    R=Row
	 *    S=Sticks to take from that row
	 **/
	public synchronized String computeMove() {
		if (game == null) {
			System.err.println("CODE ERROR: AI is not attached to a game.");
			return "0,0";
		}

		// Get the amount of sticks left in each row
		int[] rows = (int[]) game.getStateAsObject();
		//implement the formula


		int take; 
		int result = 0; 
		// this goes through to check for the xor value in a bit wise manor
		for(int i = 0; i < rows.length; i++){
			result = result ^ rows[i]; 
		}
		if (result == 0){
			// Just pick a random amount from a random row (that isn't 0)
			int r = ran.nextInt(rows.length);

			// Proceed from there to find first non-zero row
			int count = 0;
			while (rows[r] == 0 && count < rows.length) {
				count++;
				r = (r + 1) % rows.length;
			}

			if (count >= rows.length) {
				System.err.println("CODE ERROR: All 0s.  Game should be over.");
				return "-1,-1";
			}

			take = ran.nextInt(rows[r]) + 1;
			return r + "," + take;
		} 
		else {
			for (int j = 0; j< rows.length; ++j){
				if ((rows[j] ^ result) < rows[j]){
					take = rows[j] - (result ^ rows[j]);
					return j + "," + take;
				}
			}
			System.err.println("This should never have been reached"); 
			return "-1,-1"; 
		}
	}
}
		




		// Just pick a random amount from a random row (that isn't 0)
		