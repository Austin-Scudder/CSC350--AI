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
 * An interface whose sole purpose is to create new games (of a specific type).
 ***********************************************************/
public interface GameFactory {
    public Game newGame();
}
