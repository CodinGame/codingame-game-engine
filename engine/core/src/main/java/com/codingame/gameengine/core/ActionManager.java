package com.codingame.gameengine.core;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;

/**
 * The <code>ActionManager</code> is a helper class to handle actions. Its purpose is to give the correct <code>Action</code> that matched a player's
 * output.
 *
 */
public class ActionManager {
    /**
     * Returns a list of <b>legal</b> <code>Action</code>s for each output of the player's AI. The <code>List</code> can be empty or have less actions
     * than the player's outputs if they did not match any action.
     * 
     * @param player
     *            the player whose outputs will be handled
     * @return a list of <code>Action</code>s corresponding the a player's outputs.
     * @throws TimeoutException
     *             if the player's AI crashes or stops responding
     * @throws IllegalActionException 
     */
    public static List<Action> handlePlayerOutputs(AbstractPlayer player, List<ActionDescriptor> actionDescriptors) throws TimeoutException, IllegalActionException {
        List<Action> legalActions = new ArrayList<>();
        for (String singleCommand : player.getOutputs()) {
            legalActions.add(handlePlayerOutput(singleCommand, actionDescriptors));
        }
        return legalActions;
    }

    public static List<List<Action>> handlePlayerOutputs(AbstractPlayer player, List<ActionDescriptor> actionDescriptors, String separator) throws TimeoutException, IllegalActionException {
        List<List<Action>> legalActions = new ArrayList<>();
        for (String singleCommand : player.getOutputs()) {
            legalActions.add(handlePlayerOutput(singleCommand, actionDescriptors, separator));
        }
        return legalActions;
    }

    public static Action handlePlayerOutput(String command, List<ActionDescriptor> actionDescriptors) throws IllegalActionException {
        if (actionDescriptors == null || actionDescriptors.isEmpty()) {
            throw new RuntimeException("Actions are not set.");
        }

        try {
            return actionDescriptors.stream().map(a -> a.matches(command)).filter(Objects::nonNull).findFirst().get();
        } catch (NoSuchElementException e) {
            throw new IllegalActionException(command, actionDescriptors);
        }
    }

    public static List<Action> handlePlayerOutput(String command, List<ActionDescriptor> actionDescriptors, String separator) throws IllegalActionException {
        List<Action> legalActions = new ArrayList<>();
        for (String singleCommand : command.split(separator)) {
            legalActions.add(handlePlayerOutput(singleCommand, actionDescriptors));
        }
        return legalActions;
    }
}
