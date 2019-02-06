package com.codingame.gameengine.runner;

import java.util.Properties;

/**
 * The class to use to run local games and display the replay in a webpage on a temporary local server.
 */
public class MultiplayerGameRunner extends GameRunner {

    private int lastPlayerId = 0;
    private Long seed;
    private Properties gameParameters;

    /**
     * Once this runner is instantiated, the system property game.mode is set to "multi"
     */
    public MultiplayerGameRunner() {
        System.setProperty("game.mode", "multi");
    }

    /**
     * Sets the league level to run. The first league is 1.
     * <p>The value can also be set by setting the environment variable <code>league.level</code>.</p>
     * @param leagueLevel the league level. 1 is the lowest level and default value.
     */
  
    public void setLeagueLevel(int leagueLevel) {
      if (leagueLevel < 1 || leagueLevel >= 20) {
        throw new IllegalArgumentException("League level must be higher than 0 and lesser than 20");
      }
      System.setProperty("league.level", String.valueOf(leagueLevel));
    }

    /**
     * <p>
     * The seed is used to generated parameters such as width and height.<br>
     * If a seed is present in the given input, the input value should override the generated values.<br>
     * The seed will be sent to the GameManager.<br>
     * </p>
     * <p>
     * Typically, the seed is used to generated other parameters such as width and height, then those parameters are placed back in the
     * <code>Properties</code>. <br>
     * If those parameters are present in the given input, the input values should override the generated values.
     * </p>
     * 
     * @param seed this game's seed returned by the <code>GameManager</code> during execution
     */
    public void setSeed(Long seed) {
        this.seed = seed;
    }

    /**
     * <p>
     * The game parameters are used to pass additional information to the Game Manager.
     * </p>
     * 
     * @param gameParameters
     *            the parameters to send
     */
    public void setGameParameters(Properties gameParameters) {
        this.gameParameters = gameParameters;
    }

    private void addAgent(Agent player, String nickname, String avatar) {
        player.setAgentId(lastPlayerId++);
        player.setNickname(nickname);
        player.setAvatar(avatar);
        players.add(player);
    }

    /**
     * Adds an AI to the next game to run. You must keep any standard output for the game inside the given class.
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
     * Adds an AI to the next game to run.
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
    
    /**
     * Adds an AI to the next game to run, with the specified nickname.
     * 
     * @param playerClass
     *            the Java class of an AI for your game.
     * @param nickname
     *            the player's nickname
     */
    public void addAgent(Class<?> playerClass, String nickname) {
        addAgent(new JavaPlayerAgent(playerClass.getName()), nickname, null);
    }

    /**
     * Adds an AI to the next game to run, with the specified nickname.
     * <p>
     * The given command will be executed with <code>Runtime.getRuntime().exec()</code>.
     * 
     * @param commandLine
     *            the system command line to run the AI.
     * @param nickname
     *            the player's nickname
     */
    public void addAgent(String commandLine, String nickname) {
        addAgent(new CommandLinePlayerAgent(commandLine), nickname, null);
    }

    @Override
    protected void buildInitCommand(Command initCommand) {
        if (seed != null) {
            initCommand.addLine("seed=" + seed);
        }
        if (gameParameters != null) {
            for (Object key : gameParameters.keySet()) {
                initCommand.addLine(key + "=" + gameParameters.getProperty(key.toString()));
            }
        }
    }
}
