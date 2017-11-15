package com.codingame.core;

import java.util.Properties;

import javax.inject.Inject;

import com.codingame.core.AbstractPlayer;
import com.codingame.core.Referee;
import com.codingame.core.GameManager;

class PongPlayer extends AbstractPlayer {
    int y;
}

public class PongReferee implements Referee {

    @Inject private GameManager<PongPlayer> gameManager;

    @Override
    public Properties init(Properties p) {
        System.out.println("init");
        gameManager.setTurnMaxTime(12345);
        gameManager.setMaxTurn(65478);
        return p;
    }

    @Override
    public void gameTurn(int turnIndex) {
        System.out.println("gameTurn");
    }
}
