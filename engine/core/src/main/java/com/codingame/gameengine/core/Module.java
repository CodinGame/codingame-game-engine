package com.codingame.gameengine.core;

/**
 * A Module can be registered to the <code>GameManager</code> in order to send information to the game view or provide utility functions to the
 * Referee.
 *
 */
public interface Module {
    /**
     * Called by the game manager after calling the <code>Referee</code>'s init method.
     * 
     * The module must be registered to the game manager.
     */
    void onGameInit();

    /**
     * Called by the game manager after calling the <code>Referee</code>'s gameTurn method.
     * 
     * The module must be registered to the game manager.
     */
    void onAfterGameTurn();

    /**
     * Called by the game manager after calling the <code>Referee</code>'s onEnd method.
     * 
     * The module must be registered to the game manager.
     */
    void onAfterOnEnd();
}
