package com.codingame.gameengine.core;

/**
 * The Referee is the brain of your game, it implements all the rules and the turn order.
 *
 */
public abstract class AbstractReferee {

    /**
     * <p>
     * Called on startup, this method exists to create the initial state of the game, according to the given input.
     * </p>
     */
    abstract public void init();

    /**
     * Called on the computation of each turn of the game.
     * <p>
     * A typical game turn:
     * </p>
     * <ul>
     * <li>Send game information to each <code>Player</code> active on this turn.</li>
     * <li>Those players' code are <code>executed</code>.</li>
     * <li>Those players' inputs are read.</li>
     * <li>The game logic is applied.</li>
     * </ul>
     * 
     * @param turn
     *            which turn of the game is currently being computed.
     */
    abstract public void gameTurn(int turn);

    /**
     * <p>
     * <i>Optional.</i>
     * </p>
     * This method is called once the final turn of the game is computed.
     * <p>
     * Typically, this method is used to set the players' final scores according to the final state of the game.
     * </p>
     */
    public void onEnd() {
    }
}