package com.codingame.core;

public class PlayerTimeoutException extends Exception {

    private static final long serialVersionUID = -8569665968534367562L;

    private AbstractPlayer player;

    PlayerTimeoutException(AbstractPlayer player) {
        this.player = player;
    }

    public AbstractPlayer getPlayer() {
        return player;
    }
}
