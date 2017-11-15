package com.codingame.core;

import java.util.Properties;

public interface Referee {

    Properties init(Properties params);

    void gameTurn(int turnIndex);

    default void onEnd() {
    }
}
