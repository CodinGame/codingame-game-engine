package com.codingame.gameengine.core;

import java.util.Properties;

/**
 * The Referee is the brain of your game, it implements all the rules and the turn order.
 *
 */
public interface Referee {

    /**
     * <p>
     * Called on startup, this method exists to create the initial state of the game, according to the given input.
     * </p>
     * <p>
     * Typically, <code>params</code> contains at least a seed. <br>
     * The seed is used to generated other parameters such as width and height, then those parameters are placed in the return <code>Properties</code>
     * with the seed. <br>
     * If those parameters are present in the given input, the input values should override the generated values.
     * </p>
     * 
     * @param params
     *            a <code>Properties</code> containing input info for the referee.
     * @return a <code>Properties</code> containing game data that can be used as input for a later game.
     */
    Properties init(Properties params);

    /**
     * Called on the computation of each turn of the game.
     * <p>
     * A typical game turn:
     * <ul>
     * <li>Send game information to each <code>Player</code> active on this turn.</li>
     * <li>Those players' code are <code>executed</code>.</li>
     * <li>Those players' inputs are read.</li>
     * <li>The game logic is applied.</li>
     * </ul>
     * </p>
     * 
     * @param turn
     *            which turn of the game is currently being computed.
     */
    void gameTurn(int turn);

    /**
     * <p>
     * <i>Optional.</i>
     * </p>
     * This method is called once the final turn of the game is computed.
     * <p>
     * Typically, this method is used to set the players' final scores according to the final state of the game.
     * </p>
     */
    default void onEnd() {
    }
}