package com.codingame.gameengine.core;

/**
 * The representation of a player's AI during the game's execution.
 */
abstract public class AbstractMultiplayerPlayer extends AbstractPlayer {

    private boolean active = true;

    /**
     * Returns an integer that will be converted into the player's real color by the viewer.
     * 
     * @return the player's color token.
     */
    public final int getColorToken() {
        return -(this.index + 1);
    }

    /**
     * Returns true is the player is still active in the game (can be executed).
     * 
     * @return true is the player is active.
     */
    public final boolean isActive() {
        return this.active;
    }

    /**
     * Get player index from 0 (included) to number of players (excluded).
     * 
     * @return the player index.
     */
    public final int getIndex() {
        return super.getIndex();
    }

    /**
     * Get current score.
     * 
     * @return current player score
     */
    public final int getScore() {
        return super.getScore();
    }

    /**
     * Set current score. This is used to rank the players at the end of the game.
     * 
     * @param score current player score
     */
    public final void setScore(int score) {
        super.setScore(score);
    }

    /**
     * Deactivate a player. The player can't play after this and is no longer in the list of active players.
     */
    public final void deactivate() {
        this.deactivate(null);
    }

    /**
     * Deactivate a player and adds a tooltip with the reason. The player can't play after this and is no longer in the list of active players.
     * 
     * @param reason
     *            Message to display in the tooltip.
     */
    public final void deactivate(String reason) {
        this.active = false;
        if (reason != null) {
            gameManagerProvider.get().addTooltip(new Tooltip(index, reason));
        }
    }
}
