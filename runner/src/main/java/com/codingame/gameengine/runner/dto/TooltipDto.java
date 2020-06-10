package com.codingame.gameengine.runner.dto;

/**
 * A data transfer object for tooltips displayed on the progress bar of the viewer.
 * <p>
 * Used to show important events during a game such as point scoring.
 * </p>
 * Used internally.
 */
@SuppressWarnings("javadoc")
public class TooltipDto {
    public int turn;
    public String text;
    public Integer event;

    public TooltipDto(String text, int eventId, int turn) {
        this.turn = turn;
        this.text = text;
        this.event = eventId;
    }
}
