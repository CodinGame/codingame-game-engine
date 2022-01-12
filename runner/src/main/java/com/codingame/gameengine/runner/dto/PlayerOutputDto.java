package com.codingame.gameengine.runner.dto;

public final class PlayerOutputDto {

    public final String output; // nullable!
    public final long timeSpent; // in nanoseconds

    public PlayerOutputDto(final String output, final long timeSpent) {
        this.output = output;
        this.timeSpent = timeSpent;
    }

}
