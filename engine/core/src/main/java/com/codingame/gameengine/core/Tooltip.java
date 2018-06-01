package com.codingame.gameengine.core;

/**
 * The data for a tooltip which appears on the progress bar of the replay of a game to give information about significant game events. You may create
 * several tooltips for the same turn.
 */
public class Tooltip {
    int player;
    String message;

    /**
     * Creates a tooltip which will appear on the replay of the current game. The tooltip will have the same color as one of the players.
     * <p>
     * The message to display is typically no longer than 30 characters.
     * </p>
     * 
     * @param player
     *            the index of the player the tooltip information is about.
     * @param message
     *            the message to display in the tooltip.
     */
    public Tooltip(int player, String message) {
        this.player = player;
        this.message = message;
    }
}