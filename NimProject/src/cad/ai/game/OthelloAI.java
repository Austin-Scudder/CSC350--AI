/*******************
 * Christian A. Duncan
 * Modified by: Nicholas Molina, Austin Scudder, and Matt Jags
 * CSC350: Intelligent Systems
 * Spring 2019
 *
 * AI Game Client
 * This project is designed to link to a basic Game Server to test
 * AI-based solutions.
 * 
 * OthelloAI:
 *    This class is the main AI system for the Othello game.
 *
 * See README file for more details.
 ********************/
package cad.ai.game;

import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;

/***********************************************************
 * The AI system for a OthelloGame.
 *   Most of the game control is handled by the Server but
 *   the move selection is made here - either via user or an attached
 *   AI system.
 ***********************************************************/
public class OthelloAI extends AbstractAI {
    public static class Action {
        /*
            Borrowing this from OthelloGame. We want to associate a value 
            assessment to an action; the easiest thing is just another field.
            Similarly, we add a new constructor so we can pass a move value with out
            functions that return Actions.
        */
        public int row;
        public int col;
        public int value = 0;
        public String history = "";
        public Action(int r, int c) { row = r; col = c; }
        public Action(int value) {this.value = value;}

        public String toString() { return "" + row + (char) (col+'a'); }
    }
    
    
    public OthelloGame game;  // The game that this AI system is playing
    protected Random ran;
    private final int[] rowCheckValues = {1, 1, 0, -1, -1, -1, 0, 1};
    private final int[] columnCheckValues = {0, -1, -1, -1, 0, 1, 1, 1};
    private byte goodVectors = 0b00000000;
    private int tempRow = 0;
    private int tempColumn = 0;
    private char playerPiece = 'O';
    private char opponentPiece = 'X';
    private char workingPlayerPiece = 'O';
    private char workingOpponentPiece = 'X';
    private boolean isPlayerInitialized = false;
    private int totalValue = 0;
    private int player = 0;
    private int opponent = 0;
    
    private Thread thinker;
    private Thread playerThread;
    private Action currentBestMove;
    
    /*
        Floats instead of doubles for the timers. My intuition is that whatever margin is lost by
        the lesser precision will be more than made up for by faster processing.
        
        And if I'm wrong, it's hardly likely to be the error that loses us a game.
    */
    private float timeRemaining = 88.0f;
    private float thinkingTime = 0.0f;
    private float thinkingTimeThreshold = 0.0f;
    private final int MID_GAME_THRESHOLD = 30; // Once this many pieces are placed, we're in the most
                                               // crucial part of the game and need more time to think.
                                               // This will need testing to find the optimum number.

    public OthelloAI() {
        game = null;
        ran = new Random();
        thinker = new Thread(new Runnable() {
                @Override
                public void run() {
                    //ACTUALLY DO STUFF HERE
                    //More specifically, compute a best move.
                    updateBestMove();
                }
            }        
        );
        playerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //ALSO DO STUFF HERE LIKE WAIT FOR COMPUTE MOVE OR SOMETHING HOWEVER THAT WORKS.
                //WE MAY NOT HAVE TO DO THIS
            }
        });
        thinker.start();
    }

    public synchronized void attachGame(Game g) {
        game = (OthelloGame) g;
    }
    
    /*
        PlayerThread
        ThinkerThread
        
        PlayerThread:
        
        
        
        ComputeBestMove
        GetTheirActions
        GetTheirBestMove(FiveInFuture)
        ComputeBestMoveFromThere
        
    */
    
    /**
     * Returns the Move as a String "rc" (e.g. 2b)
     **/
    public synchronized String computeMove() {
        if (game == null) {
            System.err.println("CODE ERROR: AI is not attached to a game.");
            return "0a";
        }
        
        if (!isPlayerInitialized) {
            player = game.getPlayer();
            opponent = player ^ 1;
            playerPiece = player == 0 ? 'X' : 'O';
            opponentPiece = player == 0 ? 'O' : 'X';
            timeRemaining = 88.0f; // We get 90 seconds, but we're accounting for margin of error.
            isPlayerInitialized = true;
        }
	
        char[][] board = (char[][]) game.getStateAsObject();

        ArrayList<Action> actions = getActions(player, board);
        
        return getBestMove(board, Integer.MIN_VALUE, Integer.MAX_VALUE, 15).toString();
    }
    
    private void updateBestMove() {
        
    }

    private int getBoardValue(char[][] board) {
        int score = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == playerPiece) {
                    score++;
                } else if (board[i][j] == opponentPiece) {
                    score--;
                }
            }
        }
        return score;
    }
    
    /*
        Implement thinker thread
            Run to an arbitrary depth until called upon
            Once called upon, get a time limit
            Run until depth is finished or time limit is reached
            Set currentbestmove
        
        Determine time limit
        
        Refine board evaluation.
    */
    
    /*
        So this is fun. How do we modify alpha beta (a depth first search) to function the way we want
        as a breadth first search?
        
        Moves are visualized as a tree, but we can't just navigate to different parts of the tree.
        
        Or can we?
        
        The current board state is the root. Each child is that board state plus an action. Each child 
        of that is the current board state plus an action plus an action.
        
        We keep that tree. We keep filling it in. When we receive a new board state, that board state
        becomes our new root.
        
        Each node knows its children. Its children have values. We evaluate the board state as we
        attach the children. Once we finish a row down to a depth (maybe starting only from some useful
        depth like 8 or 10), use the leaf node values to find the best move via alpha-beta.
        
        Why is this good? This is a task we can maintain as our opponent is thinking. We keep a knowledge
        base of future board states so that when we get the board state with the next move, we already
        have calculated its future moves. Ideally, this advantage builds upon itself: the deeper our tree
        goes, the deeper our next move's tree is, and thus the more accurate the board evaluation.
        
        When a move tree's row is finished, we update some data: how many leaf nodes will be in the next
        row, what the current best move is, and what depth we've reached.
        
        That first one is crucial. We need to know how close to processing the next row we are. If we've
        only just started and there are a bunch left, it might be better to cut our losses, fire off a move
        and go back to thinking rather than waste time for a marginal advantage.
    */
    
    private float setTimeLimit(char[][] board) {
        int pieces = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if ((board[i][j] == 'X') || (board[i][j] == 'O')) {
                    pieces++;
                }
            }
        }
        if (timeRemaining < 5) {
            /* 
                If we're low on time, default to enough to dash out a move quickly.
                Losing by timeout is a loss obviously, and thus is to be avoided at all costs.
                If we get down to the wire, we might squeak out a win or a tie by virtue of just not
                forfeiting: this aims for that. A tenth of a second gives a little bit of time to look
                ahead while not risking a loss by timeout.
            */ 
            return 0.1f; 
        }
        /*
            The fewer pieces on the board, the more thinking we should do, except for when we get past
            the early game. So as pieces rises, we get less and less time until we hit the midgame
            threshold, at which point we go back to thinking for longer. Theoretically most of the early
            game should be on book: anything that's not is definitely worthy of extra consideration.
            
            For now, we're going to go with a simple scale. For every empty space on the board, we take
            a tenth of a second to think about an off book move.
            
            Once we hit the midgame, we increase that to a fifth of a second per empty space.
            
            This is incredibly naive, but it should work decently well as a time apportionment for now.
        */
        
        if (pieces >= MID_GAME_THRESHOLD) {
            return (((float) (64 - pieces)) * 0.2f);
        } else {
            return (((float) (64 - pieces)) * 0.1f);
        }
    }
    
    /*private boolean isCorner(char piece, int row, int col, char[][] board) {
        if (((row == 0) && (col == 0)))
    }
    
    private boolean isWallTerritory(char piece, int row, int col, char[][] board) {
        
    }*/

    private Action getBestMove(char[][] board, int alpha, int beta, int depth) {
        ArrayList<Action> actions = getActions(player, board);
        if (depth < 0) {
            return new Action(getBoardValue(board));
        }
        if (actions.size() == 0) {
            actions = getActions(opponent, board);
            if (actions.size() == 0) {
                return new Action(getBoardValue(board));
            } else {
                return getWorstMove(board, alpha, beta, depth - 1);
            }
        }
        Action bestAction = new Action(Integer.MIN_VALUE);
        for (int i = 0; i < actions.size(); i++) {
            char[][] workingBoard = copyBoard(board);
            processMove(playerPiece, actions.get(i).row, actions.get(i).col, workingBoard);
            actions.get(i).value = getWorstMove(workingBoard, alpha, beta, depth - 1).value;
            bestAction = maxAction(bestAction, actions.get(i));
            if (bestAction.value >= beta) {
                return bestAction;
            }
            alpha = Math.max(alpha, bestAction.value);
        }
        return bestAction;
    }
    
    private Action getWorstMove(char[][] board, int alpha, int beta, int depth) {
        ArrayList<Action> actions = getActions(opponent, board);
        if (depth < 0) {
            return new Action(getBoardValue(board));
        }
        if (actions.size() == 0) {
            actions = getActions(player, board);
            if (actions.size() == 0) {
                return new Action(getBoardValue(board));
            } else {
                return getBestMove(board, alpha, beta, depth - 1);
            }
        }
        Action worstAction = new Action(Integer.MAX_VALUE);
        for (int i = 0; i < actions.size(); i++) {
            char[][] workingBoard = copyBoard(board);
            processMove(opponentPiece, actions.get(i).row, actions.get(i).col, workingBoard);
            actions.get(i).value = getBestMove(workingBoard, alpha, beta, depth - 1).value;
            worstAction = minAction(worstAction, actions.get(i));
            if (worstAction.value <= alpha) {
                return worstAction;
            }
            beta = Math.min(beta, worstAction.value);
        }
        return worstAction;
    }
    
    
    /*
        This is probably more optimal to put in the above function, but
        it's a bit cleaner to have them here right now.
    */
    private Action maxAction(Action action1, Action action2) {
        if (action1.value >= action2.value) {return action1;}
        else {return action2;}
    }
    
    private Action minAction(Action action1, Action action2) {
        if (action1.value <= action2.value) {return action1;} 
        else {return action2;}
    }
    
    private char[][] copyBoard(char[][] board) {
        char[][] newBoard = new char[board.length][board.length]; // This only works because we know the board is square.
        for (int i = 0; i < board.length; i++) { // Likewise
            newBoard[i] = Arrays.copyOf(board[i], board.length);
        }
        return newBoard;
    }
    
    /*
        These next three are just c/ped and modified from OthelloGame.java, so Professor, thank yourself.
        
        In short, we need to do all these operations on our own deep copy of the board. All of these functions
        in OthelloGame work with the current actual board state. We need our own board for hypotheticals.
        
        We could create a new OthelloGame and modify that. However, copying an array is immensely less overhead than
        creating an entirely new object; by definition, creating a new OthelloGame will include, at the least, copying
        the current board. In the interest of speed, we're using the same functions OthelloGame does, but modified to
        consider a hypothetical board. Since they already consider a board object, just adding in the board as an 
        additional argument does what we need.
    */
    
    /**
     * Get the various (valid) actions that are possible 
     * @param player - which player is moving
     * @return The Actions possible.
     **/
    public ArrayList<Action> getActions(int player, char[][] board) {
        ArrayList<Action> result = new ArrayList<Action>();
        char symbol = (player == 0) ? 'X' : 'O';
	
        // Go through EVERY spot on the board
        for (int r = 0; r < board.length; r++)
            for (int c = 0; c < board[r].length; c++)
                // Determine if this board location yields a valid move
                if (isValidMove(symbol, r, c, board))
                    result.add(new Action(r,c));

        return result;
    }
    
     /**
     * Is this board location a valid move?
     * @param symbol The symbol to place X or O
     * @param row The row to place symbol
     * @param col The col to place symbol
     * @returns True if this location creates at least ONE flip option.
     **/
    public boolean isValidMove(char symbol, int row, int col, char[][] board) {
        return
            board[row][col] == ' ' &&       // Space is open
            (flipDirection(symbol, row, col, -1,  0, false, board) ||   // NORTH
             flipDirection(symbol, row, col, +1,  0, false, board) ||   // SOUTH
             flipDirection(symbol, row, col,  0, -1, false, board) ||   // WEST
             flipDirection(symbol, row, col,  0, +1, false, board) ||   // EAST
             flipDirection(symbol, row, col, -1, -1, false, board) ||   // NW
             flipDirection(symbol, row, col, -1, +1, false, board) ||   // NE
             flipDirection(symbol, row, col, +1, -1, false, board) ||   // SW
             flipDirection(symbol, row, col, +1, +1, false, board));
    }

    /**
     * Try to flip the pieces in the given direction
     * @param flip True if it should FLIP, False if it should just SEE if it can flip
     **/
    private boolean flipDirection(char symbol, int row, int col, int dr, int dc, boolean flip, char[][] board) {
        int r, c, count;
        boolean flipped = false;
        for (r = row+dr, c = col+dc, count = 0; r >= 0 && r < board.length && c >= 0 && c < board.length && board[r][c] != ' ';
             r += dr, c += dc, count++) {
            if (board[r][c] == symbol) {
                // Found one
                if (count > 0 && flip) 
                    // Flip the ones over between the two (if any!)
                    for (r -= dr, c -= dc; r != row || c != col; r -= dr, c -= dc) {
                        board[r][c] = symbol;
                    }
                return count > 0;
            }
        }
        return false;
    }
    
    public char[][] processMove(char piece, int row, int col, char[][] board) {
        //char symbol = (p == 0) ? 'X' : 'O';
	
        //if (board[row][col] != ' ') return false;  // Spot not available
        boolean flipped = false;  // Have we flipped ANY pieces?

        // Check if we can flip in every one of the 8 directions
        flipped |= flipDirection(piece, row, col, -1,  0, true, board);   // NORTH
        flipped |= flipDirection(piece, row, col, +1,  0, true, board);   // SOUTH
        flipped |= flipDirection(piece, row, col,  0, -1, true, board);   // WEST
        flipped |= flipDirection(piece, row, col,  0, +1, true, board);   // EAST
        flipped |= flipDirection(piece, row, col, -1, -1, true, board);   // NW
        flipped |= flipDirection(piece, row, col, -1, +1, true, board);   // NE
        flipped |= flipDirection(piece, row, col, +1, -1, true, board);   // SW
        flipped |= flipDirection(piece, row, col, +1, +1, true, board);   // SE

        if (flipped) board[row][col] = piece;  // True - place the actual piece
        return board;  // True if ANY Of the directions were true.
    }
 
    
    private boolean isValidSpace(char[][] board, int row, int column, char desiredPiece) {
        if ((row >= board.length) || (column >= board.length) || (row < 0) || (column < 0)) {
            return false;
        }
        if (board[row][column] != desiredPiece) {
            return false;
        }
        return true;
    }
    
    public synchronized void displayState(char[][] board) {
        if (board == null) {
            System.out.println("No state yet to display...");
            return;
        }

        // Print the column header
        System.out.print(" ");
        for (int c = 0; c < board[0].length; c++)
            System.out.print(" " + (char) (c+'a'));
        System.out.println();

        // Print each row (with row header)
        for (int r = 0; r < board.length; r++) {
            System.out.print(r);
            for (int c = 0; c < board[r].length; c++)
                System.out.print(" " + board[r][c]);
            System.out.println(" " + r);
        }

        // Print the column header
        System.out.print(" ");
        for (int c = 0; c < board[0].length; c++)
            System.out.print(" " + (char) (c+'a'));
        System.out.println();

        // Display Current Score and whose turn it is...
        //computeScore();
        //System.out.println("Score: X=" + homeScore + " O="+ awayScore);
        //System.out.println("Turn: " + ((turn == 0) ? "Home (X)" : "Away (O)"));
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
        isPlayerInitialized = false;
    }

    /**
     * Shutdown the AI - allowing it to save its learned experience
     **/
    @Override
    public synchronized void end() {
        // Probably don't need anything here at the moment since it isn't LEARNING anything
    }
}
