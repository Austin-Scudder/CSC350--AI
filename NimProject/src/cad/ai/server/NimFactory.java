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
 * An instance of a GameFactory.  A very simple NimGame.
 ***********************************************************/
public class NimFactory implements GameFactory {
    public Game newGame() { return new NimGame(); }
}
