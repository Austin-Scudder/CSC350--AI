/*******************
 * Christian A. Duncan
 * Modified by Nicholas Molina
 * CSC350: Intelligent Systems
 * Spring 2019
 *
 * AI Game Client
 * This project is designed to link to a basic Game Server to test
 * AI-based solutions.
 * See README file for more details.
 ********************/

/*
 * "Optimal" strategy derived from diagrams at https://en.wikipedia.org/wiki/File:Tictactoe-X.svg
 * and https://en.wikipedia.org/wiki/File:Tictactoe-O.svg.
 * 
 * For reasoning behind the scare quotes, see comments in the giant switch statement below.
 * 
 * A phrase which really, in retrospect, should have been its own warning.
 */
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


		/*
		 * We want to find what move we're on so we can pick the proper response.
		 * Ten minus the number of empty spaces will equal whatever move we're on;
		 * in other words, whether it's the first, second, etc. piece to be placed.
		 */
		move = 10;

		for (int i = 0; i < board.length; i++) {
			if (board[i] == ' ') {
				move -= 1;
			}
		}

		switch (move) {
			case 1:  
				/*
				 *  Optimally, X should start in the corner. Both corner and center
				 *  are possible to win/tie from, but corner gives O the most chances
				 *  to screw up.
				 */
				return "0";
			case 2:
				/*
				 * If X took center, we want a corner. If X didn't take corner, then O
				 * wants the center as the other "best" spot.
				 */
				if (board[4] == 'X') {return "0";} else {return "4";}
			case 3:  
				/*
				 * We know X must be in the upper left corner.
				 * Depending on what O did, we want to maximize our flexibility, taking a spot that sets us
				 * up for a win next turn, while also minimizing their options: in other words, the spot that
				 * O must take to block us doesn't enable them to set up for a win.
				 */
				if ((board[1] == 'O') || (board[2] == 'O')) {return "3";}
				if ((board[3] == 'O') || (board[4] == 'O') || (board[6] == 'O')) {return "1";}
				if (board[5] == 'O') {return "4";}
				if ((board[7] == 'O') || (board[8] == 'O')) {return "2";}
				break;
			case 4:  
				/*
				 * This is where it all falls apart. Moves 4, 5, and 6 are all attempting to parse all
				 * of the possible permutations of moves thus far; a number which, if I recall my
				 * combinatorics properly, equals "a lot".
				 * 
				 * That's not to say it was impossible. I just didn't do it right: in reading off of the
				 * diagrams, I made some mistakes. They're nice art pieces, but not necessarily the best
				 * tools for the job.
				 */
				if (board[4] == 'O') {
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
				} else {
					if (board[1] == 'X') {return "7";}
					if (board[2] == 'X') {return "6";}
					if (board[3] == 'X') {return "5";}
					if (board[5] == 'X') {return "3";}
					if (board[6] == 'X') {return "2";}
					if (board[7] == 'X') {return "1";}
					if (board[8] == 'X') {return "2";}
				}
			case 5:  
				if (board[1] == 'X') {
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
					}
				}
				break;
			case 6:  
				if (board[4] == 'O') {
					if (board[0] == 'O') {
						if (board[8] == ' ') {
							return "8";
						} else {
							if (board[5] == 'X') {
								return "2";
							} else if (board[7] == 'X') {
								return "6";
							} else {
								return "7";
							}
						}
					}
					if (board[1] == 'O') {
						if (board[7] == ' ') {
							return "7";
						} else {
							if (board[8] == 'X') {
								return "6";
							} else if (board[6] == 'X') {
								return "8";
							} else {
								return "3";
							}
						}
					}
					if (board[2] == 'O') {
						if (board[6] == ' ') {
							return "6";
						} else {
							if (board[3] == 'X') {
								return "0";
							} else {
								return "3";
							}
						}
					}
					if (board[3] == 'O') {
						if (board[5] == ' ') {
							return "5";
						} else {
							if (board[8] == 'X') {
								return "2";
							} else if (board[1] == ' ') {
								return "1";
							} else {
								return "8";
							}
						}
					}
					if (board[5] == 'O') {
						if (board[3] == ' ') {
							return "3";
						} else {
							if (board[6] == 'X') {
								return "0";
							}
							if (board[0] == 'X') {
								return "6";
							}
							if (board[1] == ' ') {
								return "1";
							}
						}
					}
					if (board[6] == 'O') {
						/*
						 * THIS IS WHERE THE PROBLEM WAS!
						 * 
						 * I'm simplifying a little. This isn't the only mistake.
						 * 
						 * But it's one of The Big ones.
						 * 
						 *   |   |   
						 *   |   | X
						 *   |   |   
						 *---+---+---
						 *   |   |   
						 *   | O |  
						 *   |   |   
						 *---+---+---
						 *   |   |   
						 * O | X | X
						 *   |   |   
						 *   
						 * That should look right: if it doesn't, it's because you're like me and use
						 * a variable-width font. Anyway, so the Learner would lead off with X in 8:
						 * the corner, as it should. The Trainer would respond with O in the middle, as it
						 * should. The Learner would very quickly pick up that it needed to set up a win,
						 * and so would pick X in a side adjacent to its previous pick. Hence, X in 7.
						 * 
						 * The Trainer, naturally, would set up its own win while going for the block by
						 * playing O in 6; the Learner would also sensibly respond with X in 2.
						 * 
						 * What happens next? You'd assume that the correct move would be to play O in 5,
						 * to continue the back and forth, at which point X would play in 3, and we'd have
						 * a tie.
						 * 
						 * About that...
						 */
						if (board[2] == ' ') {
							return "2";
						} else {
							if (board[1] == 'X') {
								return "0";
							}
							if (board[5] == 'X') {
								return "8";
							} else {
								/*
								 * Right here. Not, to be clear, that the above lines of code are prize-winning.
								 * 
								 * My misreading of the O player's diagram here meant I missed a couple of the
								 * possibilities, and so never put anything in for this game state. The Trainer 
								 * would go on to play O in 1, a completely nonsensical move on every level. 
								 * The Learner would, naturally, very quickly learn to highly value the ensuing 
								 * winning move, and so would take it.
								 * 
								 * Every time.
								 * 
								 * My patch was to attempt to fix this oversight, except for one problem?
								 * 
								 * This oversight? Wasn't the first one. This was the third or fourth accidental
								 * lose state I put in, and after enough whack-a-mole, I missed this one until
								 * it was too late.
								 */
								return "1";
							}
						}
					}
					if (board[7] == 'O') {
						if (board[1] == ' ') {
							return "1";
						} else {
							if (board[0] == 'X') {
								if (board[2] == ' ') {return "2";	}
							}
							if (board[2] == 'X') {
								if (board[0] == ' ') {return "0";}
							}
						}
					}
					if (board[8] == 'O') {
						if (board[0] == ' ') {
							return "0";
						} else {
							if (board[3] == 'X') {
								return "6";
							} else if (board[1] == 'X') {
								return "2";
							} else {
								return "3";
							}
						}
					}
				} else { // Middle is X, upper left is O
					if (board[1] == 'O') {
						if (board[2] == ' ') {
							return "2";
						} else {
							return "6";
						}
					}
					if (board[2] == 'O') {
						if (board[1] == ' ') {
							return "1";
						} else {
							return "7";
						}
					}
					if (board[3] == 'O') {
						if (board[6] == ' ') {
							return "6";
						} else {
							return "2";
						}
					}
					if (board[5] == 'O') {
						if (board[1] == 'X') {return "7";}
						if (board[2] == 'X') {return "6";}
						if (board[6] == 'X') {return "2";}
						if (board[7] == 'X') {return "1";}
						if (board[8] == 'X') {return "1";}
					}
					if (board[6] == 'O') {
						if (board[3] == ' ') {return "3";} 
						else {return "5";}
					}
					if (board[7] == 'O') {
						if (board[2] == 'X') {return "6";}
						if (board[3] == 'X') {return "5";}
						if (board[5] == 'X') {return "3";}
						if (board[6] == 'X') {return "2";}
						if (board[8] == 'X') {return "3";}
					}
				}
				break;
			case 7:  
				/*
				 * After this point, I tried my best to cover all of the possibilities. I decided if there was
				 * something I missed, then it would just fall through to picking a move at random, which
				 * wouldn't be so bad.
				 * 
				 * Right?
				 * 
				 * Well, actually, from here on out, that's fine: the purpose of this trainer was to teach
				 * the AI how to play optimally by forcing it to respond to the most optimal moves.
				 * 
				 * Diversifying the endgame possibilities would still eventually result in the AI picking the
				 * best move.
				 * 
				 * The mistake was made above: if you didn't see it, you probably skipped my comment. Just
				 * Control+F "THIS IS WHERE THE PROBLEM WAS".
				 * 
				 * Because the trainer never made it here (especially as O), the learner never picked up
				 * how to play the endgame against an optimal opponent, which caused it to figuratively
				 * crash and burn, hard.
				 * 
				 * An AI that learned ten million games against this file wasn't competent. In fact, this
				 * was so damaging that an AI that started blank and just ran a few hundred thousand
				 * against the random player actually did better, just because it had seen a more diverse
				 * set of game states.
				 */
				if (board[1] == 'X') { // X in 0, 1. O in 2, 3
					if (board[4] == 'O') { // X in 6, O in 4
						if (board[3] == ' ') {return "3";}
						else {return "5";}
					} else {
						if (board[7] == 'O') {return "8";}
						else {return "7";}
					}
				}
				if (board[2] == 'X') { // X in 0, 2, 4
					if (board[4] == 'O') {return "3";}
					if (board[6] == ' ') {return "6";}
					if (board[8] == ' ') {return "8";}
				}
				if (board[3] == 'X') { // X in 0, 2, 3
					if (board[5] == ' ') {return "5";}
					if (board[8] == ' ') {return "8";}
				}
				if (board[4] == 'X') { // X in 0, 2, 4
					if (board[1] == ' ') {return "1";}
					if (board[6] == ' ') {return "6";}
					if (board[8] == ' ') {return "8";}
				}
				break;
			case 8: 
				if (board[0] == 'O') {
					if (board[1] == 'O') {
						if (board[2] == ' ') {return "2";}
					}
					if (board[2] == 'O') {
						if (board[1] == ' ') {return "1";}
					}
					if (board[3] == 'O') {
						if (board[6] == ' ') {return "6";}
					}
					if (board[6] == 'O') {
						if (board[3] == ' ') {return "3";}
					}
					if (board[1] == 'X') {
						if (board[7] == ' ') {return "7";}
					}
					if (board[2] == 'X') {
						if (board[6] == ' ') {return "6";}
						if (board[5] == 'X') {
							if (board[8] == ' ') {return "8";}
						}
						if (board[8] == 'X') {
							if (board[5] == ' ') {return "5";}
						}

					}
					if (board[3] == 'X') {
						if (board[5] == ' ') {return "5";}
					}
					if (board[5] == 'X') {
						if (board[3] == ' ') {return "3";}
					}
					if (board[6] == 'X') {
						if (board[7] == 'X') {
							if (board[8] == ' ') {return "8";}
						}
						if (board[8] == 'X') {
							if (board[7] == ' ') {return "7";}
						}
					}
					if (board[7] == 'X') {
						if (board[1] == ' ') {return "1";}
						if (board[6] == 'X') {
							if (board[8] == ' ') {return "8";}
						}
						if (board[8] == 'X') {
							if (board[6] == ' ') {return "6";}
						}
					}
					if (board[8] == 'X') {
						if (board[7] == 'X') {
							if (board[6] == ' ') {return "6";}
						}
						if (board[6] == 'X') {
							if (board[7] == ' ') {return "7";}
						}
						if (board[2] == 'X') {
							if (board[5] == ' ') {return "5";}
						}
						if (board[5] == 'X') {
							if (board[2] == ' ') {return "2";}
						}
					}
				} else { // if upper left isn't an O then there must be an O in the center
					if (board[0] == 'O') {
						if (board[8] == ' ') {return "8";}
					}
					if (board[1] == 'O') {
						if (board[7] == ' ') {return "7";}
					}
					if (board[2] == 'O') {
						if (board[6] == ' ') {return "6";}
					}
					if (board[3] == 'O') {
						if (board[5] == ' ') {return "5";}
					}
					if (board[5] == 'O') {
						if (board[3] == ' ') {return "3";}
					}
					if (board[6] == 'O') {
						if (board[2] == ' ') {return "2";}
					}
					if (board[7] == 'O') {
						if (board[1] == ' ') {return "1";}
					}
					if (board[8] == 'O') {
						if (board[0] == ' ') {return "0";}
					}
				} // That should be everything: if there's no place to win or to stop losing, then the move doesn't matter.
				/*
				 * That above comment was written during coding, not after the fact like the block 
				 * comments. Don't hold back the laughter.
				 * 
				 * Everything from here on out is the default random AI behavior, kept as a fail-safe
				 * in case I missed something.
				 */
				break;
			default:
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
