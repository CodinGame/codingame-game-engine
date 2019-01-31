package com.codingame.gameengine.runner.dto;

/**
 * A data class for tooltips displayed on the progress bar of the viewer.
 * <p>
 * Used to show important events during a game such as point scoring.
 * </p>
 */
@SuppressWarnings("javadoc")
public class Tooltip {
    int turn;
    String text;
    Integer event;

    public Tooltip(String text, int eventId, int turn) {
        this.turn = turn;
        this.text = text;
        this.event = eventId;
    }
}
