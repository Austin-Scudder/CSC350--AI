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

import cad.ai.game.*;

/***********************************************************
 * An instance of a GameFactory.  An Othello Game.
 ***********************************************************/
public class OthelloFactory implements GameFactory {
    int dim;
    public OthelloFactory(int dim) { this.dim = dim; }
    public Game newGame() { return new OthelloGame(-1, null, null, true, 1, dim); }
}
