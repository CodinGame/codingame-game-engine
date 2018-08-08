package com.codingame.gameengine.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.inject.Singleton;

/**
 * The <code>AbstractMultiplayerPlayer</code> takes care of running each turn of a multiplayer game and computing each visual frame of the replay. It
 * provides many utility methods that handle instances of your implementation of AbstractMultiplayerPlayer.
 *
 * @param <T>
 *            Your implementation of AbstractMultiplayerPlayer
 */
@Singleton
public class SoloGameManager<T extends AbstractSoloPlayer> extends GameManager<T> {

    private List<String> testCase = new ArrayList<>();

    @Override
    protected void readGameProperties(InputCommand iCmd, Scanner s) {
        if (iCmd.lineCount > 0) {
            for (int i = 0; i < (iCmd.lineCount - 1); i++) {
                testCase.add(s.nextLine());
            }
        }
    }

    /**
     * Returns the current test case the game got at initialization.
     * <p>
     * You can set it with the <code>SoloGameRunner</code>.
     * </p>
     * 
     * @return The test case for this game.
     */
    public List<String> getTestCaseInput() {
        return testCase;
    }

    /**
     * Get the player
     * 
     * @return player
     */
    public T getPlayer() {
        return this.players.get(0);
    }

    /**
     * Ends the game as a victory
     */
    public void winGame() {
        super.endGame();
        getPlayer().setScore(1);
    }

    /**
     * Ends the game as a fail
     */
    public void loseGame() {
        super.endGame();
        getPlayer().setScore(0);
    }

    /**
     * Ends the game as a victory with a green message.
     * 
     * @param message
     *            the message to display in green
     */
    public void winGame(String message) {
        super.endGame();
        super.addToGameSummary(formatSuccessMessage(message));
        getPlayer().setScore(1);
    }

    /**
     * Ends the game as a fail with a red message
     * 
     * @param message
     *            the message to display in red
     */
    public void loseGame(String message) {
        super.endGame();
        super.addToGameSummary(formatErrorMessage(message));
        getPlayer().setScore(0);
    }

    @Override
    protected boolean allPlayersInactive() {
        return getPlayer().hasTimedOut();
    }

    @Override
    protected OutputCommand getGameSummaryOutputCommand() {
        return OutputCommand.INFOS;
    }
}
