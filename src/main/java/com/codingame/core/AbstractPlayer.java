package com.codingame.core;

import java.util.List;

public abstract class AbstractPlayer {
    private boolean active;
    private int index;
    private int score;
    private String deactivatedReason = null;
    private List<String> inputs = null;
    private List<String> outputs = null;
    
    // Public API
    
    public final boolean isActive() {
        return active;
    }
    
    public final int getIndex() {
        return index;
    }
    public final int getScore() {
        return score;
    }
    
    public final void setScore(int score) {
        this.score = score;
    }
    
    public final void deactivate(String reason) {
        this.deactivatedReason = reason;
    }
    
    public final void setInputs(List<String> inputs) {
        this.inputs = inputs;
    }
    
    public final List<String> getOutputs() throws PlayerTimeoutException {
        return outputs;
    }
    
    void setActive(boolean active) {
        this.active = active;
    }
    
    void setIndex(int index) {
        this.index = index;
    }
    
    // Package API
    
    String getDeactivatedReason() {
        return deactivatedReason;
    }
    
    List<String> getInputs() {
        return inputs;
    }
    
    void setOutputs(List<String> outputs) {
        this.outputs = outputs;
    }
}
