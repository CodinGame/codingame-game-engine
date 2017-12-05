package com.codingame.gameengine.core;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provider;

public abstract class AbstractPlayer {
    @Inject Provider<GameManager<AbstractPlayer>> gameManagerProvider;
    
    @SuppressWarnings("serial")
    public static class TimeoutException extends Exception {

    }

    private int index;
    private List<String> inputs = new ArrayList<>();
    private List<String> outputs;
    private boolean active = true;
    private boolean timeout;
    private int score;
    private String reason;
    private boolean toBeExecuted ;

    // Used by referee

    public final boolean isActive() {
        return this.active;
    }

    public final int getIndex() {
        return this.index;
    }

    public final int getScore() {
        return this.score;
    }

    public final void setScore(int score) {
        this.score = score;
    }

    public final void deactivate(String reason) {
        this.active = false;
        this.reason = reason;
    }

    public final void sendInputLine(String line) {
        if (toBeExecuted) {
            throw new RuntimeException("Impossible to send new inputs after calling execute");
        }
        this.inputs.add(line);
    }

    public final void execute() {
        gameManagerProvider.get().execute(this);
        this.toBeExecuted = true;
    }

    public final List<String> getOutputs() throws TimeoutException {
        if (this.timeout) {
            throw new TimeoutException();
        }
        return this.outputs;
    }

    public abstract int getExpectedOutputLines();

    // Used by GameManager

    final void setIndex(int index) {
        this.index = index;
    }

    final String getReason() {
        return reason;
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

    final boolean isToBeExecuted() {
        return toBeExecuted;
    }
    
    final void setToBeExecuted(boolean toBeExecuted) {
        this.toBeExecuted = toBeExecuted;
    }
}
