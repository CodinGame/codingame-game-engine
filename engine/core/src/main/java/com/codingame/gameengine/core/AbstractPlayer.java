package com.codingame.gameengine.core;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * The representation of the/a player's AI during the game's execution.
 *
 */
abstract public class AbstractPlayer {
    @Inject Provider<GameManager<AbstractPlayer>> gameManagerProvider;

    /**
     * An Exception thrown by <code>getOutputs()</code> when the player's AI did not respond in time after an <code>execute()</code>.
     * <p>
     * You can change the timeout value with <code>GameManager</code>'s <code>setTurnMaxTime</code> method.
     * </p>
     *
     */
    public static class TimeoutException extends Exception {
        private static final long serialVersionUID = 42L;
    }

    protected int index;
    private List<String> inputs = new ArrayList<>();
    private List<String> outputs;
    private boolean timeout;
    private int score;
    private boolean hasBeenExecuted;
    private boolean hasNeverBeenExecuted = true;

    /**
     * Returns a string that will be converted into the real nickname by the viewer.
     * 
     * @return the player's nickname token.
     */
    public final String getNicknameToken() {
        return "$" + this.index;
    }

    /**
     * Returns a string that will be converted into the real avatar by the viewer, if it exists.
     * 
     * @return the player's avatar token.
     */
    public final String getAvatarToken() {
        return "$" + this.index;
    }

    /**
     * Get player index from 0 (included) to number of players (excluded).
     * 
     * @return the player index.
     */
    int getIndex() {
        return this.index;
    }

    /**
     * Get current score.
     * 
     * @return current player score
     */
    int getScore() {
        return this.score;
    }

    /**
     * Set current score. This is used to rank the players at the end of the game.
     * 
     * @param score
     *            The player's final score.
     */
    void setScore(int score) {
        this.score = score;
    }

    /**
     * Adds a new line to the input to send to the player on execute.
     * 
     * @param line
     *            The input to send.
     */
    public final void sendInputLine(String line) {
        if (hasBeenExecuted) {
            throw new RuntimeException("Impossible to send new inputs after calling execute");
        }
        if (this.gameManagerProvider.get().getOuputsRead()) {
            throw new RuntimeException("Sending input data to a player after reading any output is forbidden.");
        }
        this.inputs.add(line);
    }

    /**
     * Executes the player for a maximum of turnMaxTime milliseconds and store the output.
     */
    public final void execute() {
        gameManagerProvider.get().execute(this);
        this.hasBeenExecuted = true;
        this.hasNeverBeenExecuted = false;
    }

    /**
     * Gets the output obtained after an execution.
     * 
     * @return a list of output lines
     * @throws TimeoutException
     *             if the player's AI crashes or stops responding
     */
    public final List<String> getOutputs() throws TimeoutException {
        this.gameManagerProvider.get().setOuputsRead(true);
        if (!this.hasBeenExecuted) {
            throw new RuntimeException("Can't get outputs without executing it!");
        }
        if (this.timeout) {
            throw new TimeoutException();
        }
        return this.outputs;
    }

    /**
     * Returns the number of lines that the player must return.
     * 
     * If the player do not write that amount of lines before the timeout delay, no outputs at all will be available for this player. The game engine
     * will not read more than the expected output lines. Extra lines will be available for next turn.
     * 
     * @return the expected amount of lines the player must output
     */
    public abstract int getExpectedOutputLines();

    //
    // The following methods are only used by the GameManager:
    //

    final void setIndex(int index) {
        this.index = index;
    }

    final List<String> getInputs() {
        return this.inputs;
    }

    final void resetInputs() {
        this.inputs = new ArrayList<>();
    }

    final void resetOutputs() {
        this.outputs = null;
    }

    final void setOutputs(List<String> outputs) {
        this.outputs = outputs;
    }

    final void setTimeout(boolean timeout) {
        this.timeout = timeout;
    }

    final boolean hasTimedOut() {
        return timeout;
    }

    final boolean hasBeenExecuted() {
        return hasBeenExecuted;
    }

    final void setHasBeenExecuted(boolean hasBeenExecuted) {
        this.hasBeenExecuted = hasBeenExecuted;
    }

    final boolean hasNeverBeenExecuted() {
        return hasNeverBeenExecuted;
    }
}
