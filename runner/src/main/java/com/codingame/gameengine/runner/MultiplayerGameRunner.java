package com.codingame.gameengine.runner;

public class MultiplayerGameRunner extends GameRunner {

    private int lastPlayerId = 0;
    private int seed;
    private boolean isSeedSet = false;

    public MultiplayerGameRunner() {
        System.setProperty("game.mode", "multi");
    }

    public void setSeed(int seed) {
        this.seed = seed;
        isSeedSet = true;
    }

    private void addAgent(Agent player, String nickname, String avatar) {
        player.setAgentId(lastPlayerId++);
        player.setNickname(nickname);
        player.setAvatar(avatar);
        players.add(player);
    }

    /**
     * @deprecated Adds an AI to the next game to run.
     *             <p>
     * 
     * @param playerClass
     *            the Java class of an AI for your game.
     */
    public void addAgent(Class<?> playerClass) {
        addAgent(new JavaPlayerAgent(playerClass.getName()), null, null);
    }

    /**
     * Adds an AI to the next game to run.
     * <p>
     * The given command will be executed with <code>Runtime.getRuntime().exec()</code>.
     * 
     * @param commandLine
     *            the system command line to run the AI.
     */
    public void addAgent(String commandLine) {
        addAgent(new CommandLinePlayerAgent(commandLine), null, null);
    }

    /**
     * @deprecated Adds an AI to the next game to run.
     *             <p>
     * 
     * @param playerClass
     *            the Java class of an AI for your game.
     * @param nickname
     *            the player's nickname
     * @param avatarUrl
     *            the url of the player's avatar
     */
    public void addAgent(Class<?> playerClass, String nickname, String avatarUrl) {
        addAgent(new JavaPlayerAgent(playerClass.getName()), nickname, avatarUrl);
    }

    /**
     * Adds an AI to the next game to run.
     * <p>
     * The given command will be executed with <code>Runtime.getRuntime().exec()</code>.
     * 
     * @param commandLine
     *            the system command line to run the AI.
     * @param nickname
     *            the player's nickname
     * @param avatarUrl
     *            the url of the player's avatar
     */
    public void addAgent(String commandLine, String nickname, String avatarUrl) {
        addAgent(new CommandLinePlayerAgent(commandLine), nickname, avatarUrl);
    }

    @Override
    protected void setCommandInput(Command initCommand) {
        if (isSeedSet) {
            initCommand.addLine("seed=" + seed);
        }
    }
}
