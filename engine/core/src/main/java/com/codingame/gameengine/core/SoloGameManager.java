package com.codingame.gameengine.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.inject.Singleton;

@Singleton
public class SoloGameManager<T extends AbstractSoloPlayer> extends GameManager<T>{
    
    private List<String> testCase = new ArrayList<>();

    @Override
    protected void readGameProperties(InputCommand iCmd, Scanner s) {
        if (iCmd.lineCount > 0) {
            for (int i = 0; i < (iCmd.lineCount - 1); i++) {
                testCase.add(s.nextLine());
            }
        }
    }

    public List<String> getTestCase() {
        return testCase;
    }
    
    /**
     * Get the player
     * 
     * @return player
     */
    public T getPlayer() {
        return this.players.get(0);
    }
    
    /**
     * Ends the game as a victory
     */
    public void winGame() {
        super.endGame();
        getPlayer().setScore(1);
    }
    
    /**
     * Ends the game as a fail
     */
    public void loseGame() {
        super.endGame();
        getPlayer().setScore(0);
    }
    
    /**
     * Ends the game as a victory with a green message.
     * 
     * @param message the message to display in green
     */
    public void winGame(String message) {
        super.endGame();
        super.addToGameSummary(formatSuccessMessage(message));
        getPlayer().setScore(1);
    }
    
    /**
     * Ends the game as a fail with a red message
     * 
     * @param message the message to display in red
     */
    public void loseGame(String message) {
        super.endGame();
        super.addToGameSummary(formatErrorMessage(message));
        getPlayer().setScore(0);
    }

}
