package com.codingame.gameengine.core;
import java.util.Properties;

public interface Referee {

    Properties init(Properties params);

    void gameTurn(int turn);

    default void onEnd() {
    }
}