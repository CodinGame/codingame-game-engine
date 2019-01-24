package com.codingame.gameengine.module.endscreen;

import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.Module;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * The EndScreen takes care of displaying and animating an end screen with the scores of the players at the end of the game.
 * 
 */
@Singleton
public class EndScreenModule implements Module {

    private GameManager<AbstractPlayer> gameManager;
    private int[] scores;
    private String[] displayedText;
    private String titleRankingsSprite = "logo.png";

    @Inject
    EndScreenModule(GameManager<AbstractPlayer> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
    }

    /**
     * Send scores to the module
     * 
     * @param scores
     *            the scores of the different players, the index matches the player.getIndex()
     */
    public void setScores(int[] scores) {
        this.scores = scores;
    }

    /**
     * Send scores to the module
     * 
     * @param scores
     *            the scores of the different players, the index matches the player.getIndex()
     * @param displayedText
     *            the text displayed instead of the score of a player, if null or empty string for a player the score will still be displayed
     *
     */
    public void setScores(int[] scores, String[] displayedText) {
        this.scores = scores;
        this.displayedText = displayedText;
    }

    /**
     * Allows you to set the sprite used as the title of the ranking board
     * 
     * @param spriteName
     *            the name of the sprite you want to use default is "logo.png"
     */
    public void setTitleRankingsSprite(String spriteName) {
        this.titleRankingsSprite = spriteName;
    }

    /**
     * 
     * @return the name of the sprite that will be used as the title of the ranking board
     */
    public String getTitleRankingsSprite() {
        return titleRankingsSprite;
    }

    @Override
    public final void onGameInit() {
    }

    @Override
    public final void onAfterGameTurn() {
    }

    @Override
    public final void onAfterOnEnd() {
        Object[] data = { scores, titleRankingsSprite, displayedText };
        gameManager.setViewData("endScreen", data);
    }

}