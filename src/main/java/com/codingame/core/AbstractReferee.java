package com.codingame.core;

import java.util.Properties;

public abstract class AbstractReferee {

    protected abstract Properties init(Properties params);

    protected abstract void gameTurn(int turnIndex);

    protected void onEnd() {
    }
}
