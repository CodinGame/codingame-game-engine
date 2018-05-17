package com.codingame.gameengine.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * The <code>GameManager</code> takes care of running each turn of the game and computing each visual frame of the replay. It provides many utility
 * methods that handle instances of your implementation of AbstractPlayer.
 *
 * @param <T>
 *            Your implementation of AbstractPlayer
 */
@Singleton
public final class GameManager<T extends AbstractPlayer> {
    @Inject private Provider<T> playerProvider;
    @Inject private Provider<AbstractReferee> refereeProvider;
    @Inject private Gson gson;
    protected static Log log = LogFactory.getLog(GameManager.class);

    private static final int VIEW_DATA_SOFT_QUOTA = 512 * 1024;
    private static final int VIEW_DATA_HARD_QUOTA = 1024 * 1024;

    private List<T> players;
    private int maxTurns = 400;
    private int turnMaxTime = 50;
    private int firstTurnMaxTime = 1000;
    private Integer turn = null;
    private int frame = 0;
    private boolean gameEnd = false;
    private Scanner s;
    private PrintStream out;
    private Properties gameProperties;
    private AbstractReferee referee;
    private boolean newTurn;

    private List<Tooltip> currentTooltips = new ArrayList<>();
    private List<Tooltip> prevTooltips;

    private List<String> currentGameSummary = new ArrayList<>();
    private List<String> prevGameSummary;

    private JsonObject currentViewData, prevViewData;

    private int frameDuration = 1000;

    private JsonObject globalViewData = new JsonObject();

    private List<Module> registeredModules = new ArrayList<>();

    private boolean initDone = false;
    private boolean outputsRead = false;
    private int totalViewDataBytesSent = 0;

    /**
     * GameManager main loop.
     * 
     * @param is
     *            input stream used to read commands from Game
     * @param out
     *            print stream used to issue commands to Game
     */
    void start(InputStream is, PrintStream out) {
        s = new Scanner(is);
        this.out = out;
        this.referee = refereeProvider.get();

        // Init ---------------------------------------------------------------
        log.info("Init");
        InputCommand iCmd = InputCommand.parse(s.nextLine());
        int playerCount = s.nextInt();
        s.nextLine();
        players = new ArrayList<T>(playerCount);

        for (int i = 0; i < playerCount; i++) {
            T player = playerProvider.get();
            player.setIndex(i);
            players.add(player);
        }

        // create game properties
        gameProperties = new Properties();
        if (iCmd.lineCount > 0) {
            for (int i = 0; i < (iCmd.lineCount - 1); i++) {
                try {
                    gameProperties.load(new StringReader(s.nextLine()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!gameProperties.containsKey("seed")) {
            gameProperties.setProperty("seed", String.valueOf(ThreadLocalRandom.current().nextInt()));
        }

        prevViewData = null;
        currentViewData = new JsonObject();

        gameProperties = referee.init(gameProperties);
        registeredModules.forEach(Module::onGameInit);
        initDone = true;

        // Game Loop ----------------------------------------------------------
        for (turn = 0; turn < getMaxTurns() && !isGameEnd(); turn++) {
            swapInfoAndViewData();
            log.info("Turn " + turn);
            newTurn = true;
            outputsRead = false; // Set as true after first getOutputs() to forbib sendInputs

            referee.gameTurn(turn);
            registeredModules.forEach(Module::onAfterGameTurn);

            // reset players' outputs
            for (AbstractPlayer player : players) {
                player.resetOutputs();
                player.setHasBeenExecuted(false);
            }
        }

        log.info("End");

        referee.onEnd();
        registeredModules.forEach(Module::onAfterOnEnd);

        // Send last frame ----------------------------------------------------
        swapInfoAndViewData();
        newTurn = true;

        dumpView();
        dumpInfos();

        dumpGameProperties();
        dumpScores();

        s.close();
    }

    /**
     * Executes a player for a maximum of turnMaxTime milliseconds and store the output. Used by player.execute().
     * 
     * @param player
     *            Player to execute.
     */
    void execute(T player) {
        if (!this.initDone) {
            throw new RuntimeException("Impossible to execute a player during init phase.");
        }

        player.setTimeout(false);

        InputCommand iCmd = InputCommand.parse(s.nextLine());

        if (iCmd.cmd != InputCommand.Command.GET_GAME_INFO) {
            throw new RuntimeException("Invalid command: " + iCmd.cmd);
        }

        dumpView();
        dumpInfos();
        dumpNextPlayerInput(player.getInputs().toArray(new String[0]));
        dumpNextPlayerInfos(player.getIndex(), player.getExpectedOutputLines(), player.hasNeverBeenExecuted() ? firstTurnMaxTime : turnMaxTime);

        // READ PLAYER OUTPUTS
        iCmd = InputCommand.parse(s.nextLine());
        if (iCmd.cmd == InputCommand.Command.SET_PLAYER_OUTPUT) {
            List<String> output = new ArrayList<>(iCmd.lineCount);
            for (int i = 0; i < iCmd.lineCount; i++) {
                output.add(s.nextLine());
            }
            player.setOutputs(output);
        } else if (iCmd.cmd == InputCommand.Command.SET_PLAYER_TIMEOUT) {
            player.setTimeout(true);
        } else {
            throw new RuntimeException("Invalid command: " + iCmd.cmd);
        }

        player.resetInputs();
        newTurn = false;
    }

    /**
     * Swap game summary and view.
     * 
     * As these values are sent in the first call to gameManger.execute(player), this function allows to change the current view at the end of each
     * gameTurn instead of the middle of the gameTurn.
     */
    private void swapInfoAndViewData() {
        prevViewData = currentViewData;
        currentViewData = new JsonObject();

        prevGameSummary = currentGameSummary;
        currentGameSummary = new ArrayList<>();

        prevTooltips = currentTooltips;
        currentTooltips = new ArrayList<>();
    }

    private void dumpGameProperties() {
        out.println(OutputCommand.UINPUT.format(gameProperties.size()));
        for (Entry<Object, Object> t : gameProperties.entrySet()) {
            out.println(t.getKey() + "=" + t.getValue());
        }
    }

    private void dumpScores() {
        OutputData data = new OutputData(OutputCommand.SCORES);
        List<String> playerScores = new ArrayList<>();
        for (AbstractPlayer player : players) {
            playerScores.add(player.getIndex() + " " + player.getScore());
        }
        data.addAll(playerScores);
        out.println(data);
    }

    private void dumpView() {
        OutputData data = new OutputData(OutputCommand.VIEW);
        if (newTurn) {
            data.add("KEY_FRAME " + frame);
            if (turn == 0) {
                JsonObject initFrame = new JsonObject();
                initFrame.add("global", globalViewData);
                initFrame.add("frame", prevViewData);
                data.add(initFrame.toString());
            } else {
                data.add(prevViewData.toString());
            }
        } else {
            data.add("INTERMEDIATE_FRAME " + frame);
        }
        String viewData = data.toString();

        totalViewDataBytesSent += viewData.length();
        if (totalViewDataBytesSent > VIEW_DATA_HARD_QUOTA) {
            throw new RuntimeException("The amount of data sent to the viewer is too big!");
        } else if (totalViewDataBytesSent > VIEW_DATA_SOFT_QUOTA) {
            log.warn("Warning: the amount of data sent to the viewer is too big. Please try to optimize your code to send less data.");
        }

        log.info(viewData);
        out.println(viewData);

        frame++;
    }

    private void dumpInfos() {
        OutputData data = new OutputData(OutputCommand.INFOS);
        out.println(data);

        if (newTurn && getPlayerCount() > 1 && prevGameSummary != null) {
            OutputData summary = new OutputData(OutputCommand.SUMMARY);
            summary.addAll(prevGameSummary);
            out.println(summary);
        }

        if (newTurn && prevTooltips != null && !prevTooltips.isEmpty()) {
            data = new OutputData(OutputCommand.TOOLTIP);
            for (Tooltip t : prevTooltips) {
                data.add(t.message);
                data.add(String.valueOf(t.player));
            }
            out.println(data);
        }
    }

    private void dumpNextPlayerInfos(int nextPlayer, int expectedOutputLineCount, int timeout) {
        OutputData data = new OutputData(OutputCommand.NEXT_PLAYER_INFO);
        data.add(String.valueOf(nextPlayer));
        data.add(String.valueOf(expectedOutputLineCount));
        data.add(String.valueOf(timeout));
        out.println(data);
    }

    private void dumpNextPlayerInput(String[] input) {
        OutputData data = new OutputData(OutputCommand.NEXT_PLAYER_INPUT);
        data.addAll(input);
        out.println(data);
        if (log.isInfoEnabled()) {
            log.info(data);
        }
    }

    void setOuputsRead(boolean outputsRead) {
        this.outputsRead = outputsRead;
    }

    boolean getOuputsRead() {
        return this.outputsRead;
    }

    //
    // Public methods used by Referee:
    //

    /**
     * Get initial number of players.
     * 
     * @return the number of players.
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * Get all the players.
     * 
     * @return the list of players.
     */
    public List<T> getPlayers() {
        return players;
    }

    /**
     * Get all the active players.
     * 
     * @return the list of active players.
     */
    public List<T> getActivePlayers() {
        // TODO: could be optimized with a list of active players updated on player.deactivate().
        return players.stream().filter(p -> p.isActive()).collect(Collectors.toList());
    }

    /**
     * Get player with index i
     * 
     * @param i
     *            Player index
     * @return player with index i
     * @throws IndexOutOfBoundsException
     */
    public T getPlayer(int i) throws IndexOutOfBoundsException {
        return this.players.get(i);
    }

    /**
     * Specifies the frameDuration in milliseconds. Default: 1000ms
     * 
     * @param frameDuration
     *            The frame duration in milliseconds.
     */
    public void setFrameDuration(int frameDuration) {
        if (this.frameDuration != frameDuration) {
            this.frameDuration = frameDuration;
            currentViewData.addProperty("duration", frameDuration);
        }
    }

    /**
     * Returns the duration in milliseconds for the frame currently being computed.
     * 
     * @return the frame duration in milliseconds.
     */
    public int getFrameDuration() {
        return frameDuration;
    }

    /**
     * Set game end.
     */
    public void endGame() {
        this.gameEnd = true;
    }

    /**
     * Check if the game has been terminated by the referee.
     * 
     * @return true if the game is over.
     */
    public boolean isGameEnd() {
        return this.gameEnd;
    }

    /**
     * Set the maximum amount of turns. Default: 400.
     * 
     * @param maxTurns
     *            the number of turns for a game.
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    public void setMaxTurns(int maxTurns) throws IllegalStateException, IllegalArgumentException {
        if (maxTurns <= 0) {
            throw new IllegalArgumentException("Invalid maximum number of turns");
        }
        this.maxTurns = maxTurns;
    }

    /**
     * Get the maximum amount of turns.
     * 
     * @return the maximum number of turns.
     */
    public int getMaxTurns() {
        return maxTurns;
    }

    /**
     * Set the timeout delay for every players. This value can be updated during a game and will be used by execute(). Default is 50ms.
     * 
     * @param turnMaxTime
     *            Duration in milliseconds.
     * @throws IllegalArgumentException
     */
    public void setTurnMaxTime(int turnMaxTime) throws IllegalArgumentException {
        if (maxTurns <= 0) {
            throw new IllegalArgumentException("Invalid turn max time");
        }
        this.turnMaxTime = turnMaxTime;
    }

    /**
     * Get the timeout delay for every players.
     * 
     * @return the current timeout duration in milliseconds.
     */
    public int getTurnMaxTime() {
        return turnMaxTime;
    }

    /**
     * Set data for use by the viewer, for the current frame.
     * 
     * @param data
     *            any object that can be serialized in JSON using gson.
     */
    public void setViewData(Object data) {
        setViewData("default", data);
    }

    /**
     * Set data for use by the viewer, for the current frame, for a specific module.
     * 
     * @param moduleName
     *            the name of the module
     * @param data
     *            any object that can be serialized in JSON using gson.
     */
    public void setViewData(String moduleName, Object data) {
        this.currentViewData.add(moduleName, gson.toJsonTree(data));
    }

    /**
     * Set data for use by the viewer and not related to a specific frame. This must be use in the init only.
     * 
     * @param moduleName
     *            the name of the module
     * @param data
     *            any object that can be serialized in JSON using gson.
     */
    public void setViewGlobalData(String moduleName, Object data) {
        if (initDone) {
            throw new IllegalStateException("Impossible to send global data to view outside of init phase");
        }
        this.globalViewData.add(moduleName, gson.toJsonTree(data));
    }

    /**
     * Adds a tooltip for the current turn.
     * 
     * @param tooltip
     *            A tooltip that will be shown in the player.
     */
    public void addTooltip(Tooltip tooltip) {
        this.currentTooltips.add(tooltip);
    }

    /**
     * Adds a tooltip for the current turn.
     * 
     * @param player
     *            The player the tooltip information is about.
     * @param message
     *            Tooltip message.
     */
    public void addTooltip(AbstractPlayer player, String message) {
        addTooltip(new Tooltip(player.getIndex(), message));
    }

    /**
     * Add a new line to the game summary for the current turn.
     * 
     * @param summary
     *            summary line to add to the current summary.
     */
    public void addToGameSummary(String summary) {
        this.currentGameSummary.add(summary);
    }

    /**
     * Register a module to the gameManager. After this, the gameManager will call the module callbacks automatically.
     * 
     * @param m
     */
    public void registerModule(Module m) {
        registeredModules.add(m);
    }

    /**
     * Get current league level. The value can be set by using -Dleague.level=X where X is the league level.
     *
     * @return a strictly positive integer. 1 is the lowest level and default value.
     */
    public int getLeagueLevel() {
        return Integer.valueOf(System.getProperty("league.level", "1"));
    }

    /**
     * Helper function to display a colored message. Usually used at the end of the game.
     * 
     * @param message
     *            The message to display.
     * @return The formatted string.
     */
    public static String formatSuccessMessage(String message) {
        return String.format("¤GREEN¤%s§GREEN§", message);
    }

    /**
     * Helper function to display a colored message. Usually used at the end of the game.
     * 
     * @param message
     *            The message to display.
     * @return The formatted string.
     */
    public static String formatErrorMessage(String message) {
        return String.format("¤RED¤%s§RED§", message);
    }
}
