package com.codingame.gameengine.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;

/**
 * The <code>ActionManager</code> is a helper class to handle actions. 
 * Its purpose is to give the correct <code>Action</code> that matched a player's output.
 *
 */
public class ActionManager {
    private List<Action> actions = new ArrayList<>();
    
    /**
     * Set an array of <code>Action</code> a player can make.
     * 
     * @param actions the <code>Action</code>s to be handled.
     */
    public void setActions(Action... actions) {
        setActions(Arrays.asList(actions));
    }
    
    /**
     * Set a list of <code>Action</code> a player can make.
     * 
     * @param actions the <code>Action</code>s to be handled.
     */
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    /**
     * Returns a list of <b>legal</b> <code>Action</code>s for each output of the player's AI. 
     * The <code>List</code> can be empty or have less actions than the player's outputs if they did not match any action.
     * 
     * @param player the player whose outputs will be handled
     * @return a list of <code>Action</code>s corresponding the a player's outputs.
     * @throws TimeoutException if the player's AI crashes or stops responding
     */
    public List<Action> handlePlayerOutputs(AbstractPlayer player) throws TimeoutException {
        if (actions == null || actions.isEmpty()) {
            throw new RuntimeException("Actions are not set.");
        }
        
        List<String> playerOutput = player.getOutputs();

        return playerOutput.stream()
            .map(
                command -> actions.stream().filter(a -> a.matches(command)).findFirst()
                )
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }
}
