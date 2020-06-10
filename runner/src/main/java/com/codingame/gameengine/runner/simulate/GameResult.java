package com.codingame.gameengine.runner.simulate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A data class for the result of one game launch. Can be exploited for batch testing your game locally.
 * <p>
 * Each frame is divided in as many subframes as their are player agents + one initialisation subframe.
 * </p>
 */
public class GameResult {
    /**
     * Map each agent to its error output of each subframe. A player agent's key will be its index, the game code's key will be "referee".
     */
    public Map<String, List<String>> errors = new HashMap<>();
    /**
     * Map each agent to its standard output of each subframe. A player agent's key will be its index, the game code's key will be "referee".
     */
    public Map<String, List<String>> outputs = new HashMap<>();
    /**
     * The game summary output by the GameManager for each subframe.
     */
    public List<String> summaries = new ArrayList<>();
    /**
     * The view output from modules for each subframe. Each string will be a JSON Object mapping each module's name with the serialized version of
     * their output.
     */
    public List<String> views = new ArrayList<>();
    /**
     * Maps each player agent with the score returned by <code>getScore</code> from your implementation of AbstractPlayer.
     */
    public Map<Integer, Integer> scores = new HashMap<>();
    /**
     * <p>
     * For multiplayer games: a list of properties in the form <code>key=value</code> generated from <code>GameManager.getGameParameters</code>
     * <b>after the game's end</b>.
     * </p>
     * 
     * For solo games: the lines of text in the given testcase.
     * 
     */
    public List<String> gameParameters = new ArrayList<>();
    /**
     * A serialised JSON of anything stored in this game launch's metadata with <code>GameManager.putMetaData</code>.
     */
    public String metadata;
    /**
     * Any uncaught Exception that caused the game launch to crash. Includes stacktrace.
     */
    public String failCause;
    /**
     * The list of tooltips generated during this game launch.
     */
    public List<TooltipData> tooltips = new ArrayList<>();
    /**
     * The list of player agents used for this game launch.
     */
    public List<AgentData> agents = new ArrayList<>();

}
