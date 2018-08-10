package com.codingame.gameengine.core;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;

/**
 * The <code>ActionManager</code> handles actions. Its purpose is to give the correct <code>Action</code> that matched a player's
 * output.
 *
 */
public class ActionManager {
    /**
     * Returns a list of <b>legal</b> <code>Action</code>s for each output of the player's AI.
     * 
     * @param player
     *            the player whose outputs will be handled.
     * @param actionDescriptors
     *            a list of <code>ActionDescriptor</code>s corresponding the a player's possible actions.
     * @return a list of <code>Action</code>s corresponding the player's actions.
     * @throws TimeoutException
     *             if the player's AI crashes or stops responding.
     * @throws IllegalActionException
     *             if the player's AI performs an action that did not match any of the <code>ActionDescriptor</code>.
     */
    public static List<Action> handlePlayerOutputs(AbstractPlayer player, List<ActionDescriptor> actionDescriptors) throws TimeoutException, IllegalActionException {
        List<Action> legalActions = new ArrayList<>();
        for (String singleCommand : player.getOutputs()) {
            legalActions.add(handlePlayerOutput(singleCommand, actionDescriptors));
        }
        return legalActions;
    }

    /**
     * Returns a list of <b>legal</b> <code>Action</code>s for each output of the player's AI.
     * 
     * @param player
     *            the player whose outputs will be handled.
     * @param actionDescriptors
     *            a list of <code>ActionDescriptor</code>s corresponding the a player's possible actions.
     * @param separator
     *            the symbol that will be used to split actions on one line.
     * @return a list of <code>Action</code>s corresponding the player's actions.
     * @throws TimeoutException
     *             if the player's AI crashes or stops responding.
     * @throws IllegalActionException
     *             if the player's AI performs an action that did not match any of the <code>ActionDescriptor</code>.
     */
    public static List<List<Action>> handlePlayerOutputs(AbstractPlayer player, List<ActionDescriptor> actionDescriptors, String separator) throws TimeoutException, IllegalActionException {
        List<List<Action>> legalActions = new ArrayList<>();
        for (String singleCommand : player.getOutputs()) {
            legalActions.add(handlePlayerOutput(singleCommand, actionDescriptors, separator));
        }
        return legalActions;
    }

    /**
     * Returns the <b>legal</b> <code>Action</code>s that corresponds to the given <b>command</b>.
     * 
     * @param command
     *            the command that will be handled.
     * @param actionDescriptors
     *            a list of <code>ActionDescriptor</code>s corresponding the a player's possible actions.
     * @return an <code>Action</code>s corresponding the given command.
     * @throws IllegalActionException
     *             if the command did not match any of the <code>ActionDescriptor</code>.
     */
    public static Action handlePlayerOutput(String command, List<ActionDescriptor> actionDescriptors) throws IllegalActionException {
        if (actionDescriptors == null || actionDescriptors.isEmpty()) {
            throw new RuntimeException("Actions are not set.");
        }

        try {
            return actionDescriptors.stream().map(a -> a.parseInstruction(command)).filter(Objects::nonNull).findFirst().get();
        } catch (NoSuchElementException e) {
            throw new IllegalActionException(command, actionDescriptors);
        }
    }

    /**
     * Returns the <b>legal</b> <code>Action</code>s that corresponds to the given <b>command</b>.
     * 
     * @param command
     *            the command that will be handled.
     * @param actionDescriptors
     *            a list of <code>ActionDescriptor</code>s corresponding the a player's possible actions.
     * @param separator
     *            the symbol that will be used to split actions on one line.
     * @return an <code>Action</code>s corresponding the given command.
     * @throws IllegalActionException
     *             if the command did not match any of the <code>ActionDescriptor</code>.
     */
    public static List<Action> handlePlayerOutput(String command, List<ActionDescriptor> actionDescriptors, String separator) throws IllegalActionException {
        List<Action> legalActions = new ArrayList<>();
        for (String singleCommand : command.split(separator)) {
            legalActions.add(handlePlayerOutput(singleCommand, actionDescriptors));
        }
        return legalActions;
    }
}
