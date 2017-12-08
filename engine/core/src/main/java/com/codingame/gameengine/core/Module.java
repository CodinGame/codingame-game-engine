package com.codingame.gameengine.core;

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
