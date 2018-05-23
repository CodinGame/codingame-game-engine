package com.codingame.gameengine.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.inject.Singleton;

@Singleton
public class SoloGameManager<T extends AbstractPlayer> extends GameManager<T>{
    
    private List<String> testCase = new ArrayList<>();
    private boolean win;

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
    
    public void winGame() {
        super.endGame();
        win = true;
    }
    
    public void loseGame() {
        super.endGame();
        win = false;
    }

}
