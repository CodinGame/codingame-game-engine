package com.codingame.gameengine.runner.simulate;

/**
 * A data class for Agent data.
 */
public class AgentData {
    /**
     * The index of this player agent.
     */
    public int index;
    /**
     * The username used by this player agent.
     */

    public String name;
    /**
     * The avatar used by this player agent.
     */
    public String avatarUrl;

    /**
     * Player agent used in the execution of this game launch.
     * 
     * @param index
     *            The index of this player agent.
     * @param name
     *            The username used by this player agent.
     * @param avatarUrl
     *            The avatar used by this player agent.
     */
    public AgentData(int index, String name, String avatarUrl) {
        this.index = index;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

}
