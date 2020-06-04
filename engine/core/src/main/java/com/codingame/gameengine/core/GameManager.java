package com.codingame.gameengine.core;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * The <code>GameManager</code> takes care of running each turn of the game and computing each visual frame of the replay. It provides many utility
 * methods that handle instances of your implementation of AbstractPlayer.
 *
 * @param <T>
 *            Your implementation of AbstractPlayer
 */
abstract public class GameManager<T extends AbstractPlayer> {
    @Inject private Provider<T> playerProvider;
    @Inject private Provider<AbstractReferee> refereeProvider;
    @Inject private Gson gson;
    protected static Log log = LogFactory.getLog(GameManager.class);

    private static final int VIEW_DATA_TOTAL_SOFT_QUOTA = 512 * 1024;
    private static final int VIEW_DATA_TOTAL_HARD_QUOTA = 1024 * 1024;
    private static final int GAME_SUMMARY_TOTAL_HARD_QUOTA = 512 * 1024;
    private static final int GAME_SUMMARY_PER_TURN_HARD_QUOTA = 800;
    private static final int GAME_DURATION_HARD_QUOTA = 30_000;
    private static final int GAME_DURATION_SOFT_QUOTA = 25_000;
    private static final int MAX_TURN_TIME = GAME_DURATION_SOFT_QUOTA;
    private static final int MIN_TURN_TIME = 50;

    protected List<T> players;
    private int maxTurns = 200;
    private int turnMaxTime = 50;
    private int firstTurnMaxTime = 1000;
    private Integer turn = null;
    private int frame = 0;
    private boolean gameEnd = false;
    private Scanner s;
    protected PrintStream out;
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

    private Map<String, String> metadata = new HashMap<>();

    private boolean initDone = false;
    private boolean outputsRead = false;
    private int totalViewDataBytesSent = 0;
    private int totalGameSummaryBytes = 0;
    private int totalTurnTime = 0;

    private boolean viewWarning, summaryWarning;

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
        try {
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

            readGameProperties(iCmd, s);

            prevViewData = null;
            currentViewData = new JsonObject();

            referee.init();
            registeredModules.forEach(Module::onGameInit);
            initDone = true;

            // Game Loop ----------------------------------------------------------
            for (turn = 1; turn <= getMaxTurns() && !isGameEnd() && !allPlayersInactive(); turn++) {
                swapInfoAndViewData();
                log.info("Turn " + turn);
                newTurn = true;
                outputsRead = false; // Set as true after first getOutputs() to forbid sendInputs

                referee.gameTurn(turn);
                registeredModules.forEach(Module::onAfterGameTurn);

                // Create a frame if no player has been executed
                if (!players.isEmpty() && players.stream().noneMatch(p -> p.hasBeenExecuted())) {
                    execute(players.get(0), 0);
                }

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
            dumpMetadata();
            dumpScores();

            s.close();

        } catch (Throwable e) {
            dumpFail(e);
            s.close();
            throw e;
        }
    }

    abstract protected boolean allPlayersInactive();

    abstract protected void readGameProperties(InputCommand iCmd, Scanner s);

    /**
     * Executes a player for a maximum of turnMaxTime milliseconds and store the output. Used by player.execute().
     * 
     * @param player
     *            Player to execute.
     * @param nbrOutputLines
     *            The amount of expected output lines from the player.
     */
    protected void execute(T player, int nbrOutputLines) {
        try {
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
            if (nbrOutputLines > 0) {
                addTurnTime();
            }
            dumpNextPlayerInfos(player.getIndex(), nbrOutputLines, player.hasNeverBeenExecuted() ? firstTurnMaxTime : turnMaxTime);

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
        } catch (Throwable e) {
            //Don't let the user catch game fail exceptions
            dumpFail(e);
            throw e;
        }
    }

    /**
     * Executes a player for a maximum of turnMaxTime milliseconds and store the output. Used by player.execute().
     * 
     * @param player
     *            Player to execute.
     */
    void execute(T player) {
        execute(player, player.getExpectedOutputLines());
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

    protected void dumpGameProperties() {
    }

    private void dumpMetadata() {
        OutputData data = new OutputData(OutputCommand.METADATA);
        data.add(getMetadata());
        out.println(data);
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

    private void dumpFail(Throwable e) {
        OutputData data = new OutputData(OutputCommand.FAIL);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        data.add(sw.toString());
        out.println(data);
    }

    private void dumpView() {
        OutputData data = new OutputData(OutputCommand.VIEW);
        if (newTurn) {
            data.add("KEY_FRAME " + frame);
            if (turn == 1) {
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
        if (totalViewDataBytesSent > VIEW_DATA_TOTAL_HARD_QUOTA) {
            throw new RuntimeException("The amount of data sent to the viewer is too big!");
        } else if (totalViewDataBytesSent > VIEW_DATA_TOTAL_SOFT_QUOTA && !viewWarning) {
            log.warn(
                "Warning: the amount of data sent to the viewer is too big.\nPlease try to optimize your code to send less data (try replacing some commitEntityStates by a commitWorldState)."
            );
            viewWarning = true;
        }

        log.info(viewData);
        out.println(viewData);

        frame++;
    }

    private void dumpInfos() {
        OutputData data = new OutputData(OutputCommand.INFOS);
        out.println(data);

        if (newTurn && prevGameSummary != null) {
            OutputData summary = new OutputData(getGameSummaryOutputCommand());
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

    abstract protected OutputCommand getGameSummaryOutputCommand();

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

    private String getMetadata() {
        return gson.toJsonTree(metadata).getAsJsonObject().toString();
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
     * Puts a new metadata that will be included in the game's <code>GameResult</code>.
     * <p>
     * Can be used for: 
     * </p>
     * <ul>
     * <li>Setting the value of an optimization criteria for OPTI games, used by the CodinGame IDE</li>
     * <li>Dumping game statistics for local analysis after a batch run of GameRunner.simulate()</li>
     * </ul>
     * 
     * @param key
     *            the property to send
     * @param value
     *            the property's value
     */
    public final void putMetadata(String key, String value) {
        metadata.put(key, value);
    }

    /**
     * Specifies the frameDuration in milliseconds. Default: 1000ms
     * 
     * @param frameDuration
     *            The frame duration in milliseconds.
     * @throws IllegalArgumentException
     *             if frameDuration &le; 0
     */
    public void setFrameDuration(int frameDuration) {
        if (frameDuration <= 0) {
            throw new IllegalArgumentException("Invalid frame duration: only positive frame duration is supported");
        } else if (this.frameDuration != frameDuration) {
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
    protected void endGame() {
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
     * @throws IllegalArgumentException
     *             if maxTurns &le; 0
     */
    public void setMaxTurns(int maxTurns) throws IllegalArgumentException {
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
     * Set the timeout delay for every player. This value can be updated during a game and will be used by execute(). Default is 50ms.
     * 
     * @param turnMaxTime
     *            Duration in milliseconds.
     * @throws IllegalArgumentException
     *             if turnMaxTime &lt; 50 or &gt; 25000
     */
    public void setTurnMaxTime(int turnMaxTime) throws IllegalArgumentException {
        if (turnMaxTime < MIN_TURN_TIME) {
            throw new IllegalArgumentException("Invalid turn max time : stay above 50ms");
        } else if (turnMaxTime > MAX_TURN_TIME) {
            throw new IllegalArgumentException("Invalid turn max time : stay under 25s");
        }
        this.turnMaxTime = turnMaxTime;
    }
    
    /**
     * Set the timeout delay of the first turn for every player. Default is 1000ms.
     * 
     * @param firstTurnMaxTime
     *            Duration in milliseconds.
     * @throws IllegalArgumentException
     *             if firstTurnMaxTime &lt; 50 or &gt; 25000
     */
    public void setFirstTurnMaxTime(int firstTurnMaxTime) throws IllegalArgumentException {
        if (firstTurnMaxTime < MIN_TURN_TIME) {
            throw new IllegalArgumentException("Invalid turn max time : stay above 50ms");
        } else if (firstTurnMaxTime > MAX_TURN_TIME) {
            throw new IllegalArgumentException("Invalid turn max time : stay under 25s");
        }
        this.firstTurnMaxTime = firstTurnMaxTime;
    }

    /**
     * Get the timeout delay for every player.
     * 
     * @return the current timeout duration in milliseconds.
     */
    public int getTurnMaxTime() {
        return turnMaxTime;
    }
    
    /**
     * Get the timeout delay of the first turn for every player.
     * 
     * @return the first turn timeout duration in milliseconds.
     */
    public int getFirstTurnMaxTime() {
        return firstTurnMaxTime;
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
        int total = this.currentGameSummary.stream()
            .mapToInt(String::length)
            .sum();

        if (total < GAME_SUMMARY_PER_TURN_HARD_QUOTA && total + totalGameSummaryBytes < GAME_SUMMARY_TOTAL_HARD_QUOTA) {
            this.currentGameSummary.add(summary);
            totalGameSummaryBytes += total;
        } else if (!summaryWarning) {
            log.warn("Warning: the game summary is full. Please try to send less data.");
            summaryWarning = true;
        }
    }

    private void addTurnTime() {
        totalTurnTime += turnMaxTime;
        if (totalTurnTime > GAME_DURATION_HARD_QUOTA) {
            throw new RuntimeException(String.format("Total game duration too long (>%dms)", GAME_DURATION_HARD_QUOTA));
        } else if (totalTurnTime > GAME_DURATION_SOFT_QUOTA) {
            log.warn(
                String.format(
                    "Warning: too many turns and/or too much time allocated to players per turn (%dms/%dms)",
                    totalTurnTime, GAME_DURATION_HARD_QUOTA
                )
            );
        }
    }

    /**
     * Register a module to the gameManager. After this, the gameManager will call the module callbacks automatically.
     * 
     * @param module
     *            the module to register
     */
    public void registerModule(Module module) {
        registeredModules.add(module);
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
