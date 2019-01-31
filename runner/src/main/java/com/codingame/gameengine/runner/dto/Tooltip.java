package com.codingame.gameengine.runner.dto;

/**
 * A data class for tooltips displayed on the progress bar of the viewer.
 * <p>
 * Used to show important events during a game such as point scoring.
 * </p>
 */
public class Tooltip {
    int turn;
    String text;
    Integer event;

    
    /**
     * These tooltips would be displayed on the progress bar of the viewer.  
     * @param text the text displayed in the tooltip. Should be short
     * @param eventId equal to the id of on of the agents, changes the tooltips colours to the colour of that player 
     * @param turn the viewer frame on which the tooltip will be visible 
     */
    public Tooltip(String text, int eventId, int turn) {
        this.turn = turn;
        this.text = text;
        this.event = eventId;
    }
}
