/*******************
 * Christian A. Duncan
 * CSC350: Intelligent Systems
 * Spring 2019
 *
 * AI Game Client
 * This project is designed to link to a basic Game Server to test
 * AI-based solutions.
 * See README file for more details.
 ********************/
package cad.ai.game;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
    final String filename = "./Test-TTTBrain2.txt";
	int side = player();// This gets the player and is 0 if you are home 1 if you are away
	int WLT = 3; // Will be 0 for loss 1 for win 
    Stack<String> boardstate = new Stack<String>();
	
	
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
    	///change the file path for your directory. 
    			
    			//System.out.println(game.getPlayer());
    			//This decides the WLT 
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
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
			String revestate = (String) boardstate.pop();
			writer.write(revestate);
			writer.newLine();
			writer.write(WLT);
			writer.newLine();
			writer.close();
		} catch (FileNotFoundException e) {
			System.err.println("file not found");
			e.printStackTrace();
		}
		catch (IOException e) {
			System.err.println("unknown thing");
			e.printStackTrace();
		}
        // This AI probably wants to store (in a file) what
        // it has learned from playing all the games so far...
    }
}
