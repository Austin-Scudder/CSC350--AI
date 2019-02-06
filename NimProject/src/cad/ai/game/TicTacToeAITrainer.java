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

import java.util.Random;

/***********************************************************
 * The AI system for a TicTacToeGame.
 *   Most of the game control is handled by the Server but
 *   the move selection is made here - either via user or an attached
 *   AI system.
 ***********************************************************/
public class TicTacToeAITrainer extends AbstractAI {
    public TicTacToeGame game;  // The game that this AI system is playing
    protected Random ran;
    int move = 0;
    
    public TicTacToeAITrainer() {
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
        
        move = calculateMoveNumber(board);
                        
        switch (move) {
            1:  return "0";
                break;
            2:  if (board[4] == 'X') {return "0";} else {return "4"}
                break;
            3:  if ((board[1] == 'O') || (board[2] == 'O')) {return "3";}
                if ((board[3] == 'O') || (board[4] == 'O') || (board[6] == 'O')) {return "1";}
                if (board[5] == 'O') {return "4";}
                if ((board[7] == 'O') || (board[8] == 'O')) {return "2";}
                break;
            4:  if (board[4] == 'O') {
                    if (board[0] == 'X') {
                        if ((board[2] == 'X') || (board[8] == 'X')) {return "1";}
                        if (board[1] == 'X') {return "2";}
                        if (board[3] == 'X') {return "6";}
                        if (board[5] == 'X') {return "7";}
                        if (board[6] == 'X') {return "3";}
                        if (board[7] == 'X') {return "5";}
                    }
                    if (board[1] == 'X') {
                        if ((board[0] == 'X') || (board[3] == 'X')) {return "2";}
                        if ((board[2] == 'X') || (board[5] == 'X') || (board[7] == 'X')) {
                            return "0";
                        }
                        if (board[6] == 'X') {return "5";}
                        if (board[8] == 'X') {return "3";}
                    }
                    if (board[2] == 'X') {
                        if ((board[0] == 'X') || (board[6] == 'X')) {return "1";}
                        if (board[1] == 'X') {return "0";}
                        if (board[3] == 'X') {return "7";}
                        if (board[5] == 'X') {return "8";}
                        if (board[7] == 'X') {return "3";}
                        if (board[8] == 'X') {return "5";}
                    }
                    if (board[3] == 'X') {
                        if (board[0] == 'X') {return "6";}
                        if (board[1] == 'X') {return "2";}
                        if (board[2] == 'X') {return "7";}
                        if ((board[5] == 'X') || (board[6] == 'X') 
                            || (board[7] == 'X')) {return "0";}
                        if (board[8] == 'X') {return "1";}
                    }
                    if (board[5] == 'X') {
                        if (board[0] == 'X') {return "7";}
                        if ((board[1] == 'X') || (board[3] == 'X')) {return "0";}
                        if (board[2] == 'X') {return "8";}
                        if (board[6] == 'X') {return "1";}
                        if ((board[7] == 'X') || (board[8] == 'X')) {return "2";}
                    }
                    if (board[6] == 'X') {
                        if (board[0] == 'X') {return "3";}
                        if (board[1] == 'X') {return "5";}
                        if ((board[2] == 'X') || (board[5] == 'X')) {return "1";}
                        if (board[3] == 'X') {return "0";}
                        if (board[7] == 'X') {return "8";}
                        if (board[8] == 'X') {return "7";}
                    }
                    if (board[7] == 'X') {
                        if (board[0] == 'X') {return "5";}
                        if ((board[1] == 'X') || (board[3] == 'X')) {return "0";}
                        if (board[2] == 'X') {return "3";}
                        if (board[5] == 'X') {return "2";}
                        if (board[6] == 'X') {return "8";}
                        if (board[8] == 'X') {return "6";}
                    }
                    if (board[8] == 'X') {
                        if ((board[0] == 'X') || (board[3] == 'X')) {return "1";}
                        if (board[1] == 'X') {return "3";}
                        if (board[2] == 'X') {return "6";}
                        if (board[5] == 'X') {return "2";}
                        if (board[6] == 'X') {return "7";}
                        if (board[7] == 'X') {return "6";}
                    }
                }
                break;
            5:  if (board[1] == 'X') {
                    if (board[2] == 'O') {
                        if (board[4] == 'O') {
                            return "6";
                        } else {
                            return "4";
                        }
                    } else {
                        return "2";
                    }
                }
                if (board[2] == 'X') {
                    if (board[1] == 'O') {
                        if (board[4] == 'O') {
                            return "6";
                        } else {
                            return "4";
                        }
                    } else {
                        return "1";
                    }
                }
                if (board[3] == 'X') {
                    if (board[6] == 'O') {
                        return "4";
                    } else {
                        return "6";
                    }
                }
                if (board[4] == 'X') {
                    if (board[8] == 'O') {
                        return "2";
                    } else {
                        return "8";
                    ]
                }
                break;
            6:
                break;
            7:
                break;
            8:
                break;
            9:
                break;
        }

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

    /**
     * Inform AI who the winner is
     *   result is either (H)ome win, (A)way win, (T)ie
     **/
    @Override
    public synchronized void postWinner(char result) {
        // This AI probably wants to store what it has learned
        // about this particular game.
        game = null;  // No longer playing a game though.
    }

    /**
     * Shutdown the AI - allowing it to save its learned experience
     **/
    @Override
    public synchronized void end() {
        // This AI probably wants to store (in a file) what
        // it has learned from playing all the games so far...
    }
}
