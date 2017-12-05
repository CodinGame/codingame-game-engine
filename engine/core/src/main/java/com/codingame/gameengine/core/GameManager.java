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
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public final class GameManager<T extends AbstractPlayer> {
    @Inject private Provider<T> playerProvider;
    @Inject private Provider<Referee> refereeProvider;
    @Inject private Gson gson;

    private List<T> players;
    private int maxTurns = 400;
    private int turnMaxTime = 50;
    private Integer turn = null;
    private int frame = 0;
    private boolean gameEnd = false;
    private Scanner s;
    private PrintStream out;
    private Properties gameProperties;
    private Referee referee;
    private boolean newTurn;
    
    private List<Tooltip> currentTooltips;
    private List<Tooltip> prevTooltips;
    
    private List<String> currentGameSummary;
    private List<String> prevGameSummary;
    
    private JsonObject currentViewData;
    private JsonObject prevViewData;

    public void start(InputStream is, PrintStream out) {
        s = new Scanner(is);
        this.out = out;
        this.referee = refereeProvider.get();

        // Init ---------------------------------------------------------------
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
        
        prevViewData = null;
        currentViewData = createNewView(true);

        gameProperties = referee.init(playerCount, gameProperties);
        swapInfoAndViewData();
        
        // Game Loop ----------------------------------------------------------
        for (turn = 0; turn < getMaxTurns() && !isGameEnd(); turn++) {
            newTurn = true;

            referee.gameTurn(turn);

            // reset players' outputs
            for (AbstractPlayer player: players) {
                player.resetOutputs();
                player.setToBeExecuted(false);
            }

            swapInfoAndViewData();
        }

        referee.onEnd();

        // Send last frame ----------------------------------------------------
        
        newTurn = true;

        frame++;
        dumpView();
        dumpInfos();

        try {
            appendDataToEnd(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] playerScores = new String[getPlayerCount()];
        for (int i = 0; i < getPlayerCount(); i++) {
            playerScores[i] = i + " " + getPlayer(i).getScore();
        }
        OutputData data = new OutputData(OutputCommand.SCORES);
        data.addAll(playerScores);
        out.println(data);
        s.close();
    }
    
    private void swapInfoAndViewData() {
        prevViewData = currentViewData;
        currentViewData = createNewView(true);
        
        prevGameSummary = currentGameSummary;
        currentGameSummary = null;
        
        prevTooltips = currentTooltips;
        currentTooltips = null;
    }
    
    private JsonObject createNewView(boolean keyFrame) {
        JsonObject viewData = new JsonObject();
        viewData.addProperty("key", keyFrame);
        viewData.addProperty("frameNumber", frame);
        return viewData;
    }
    
    private void appendDataToEnd(PrintStream stream) throws IOException {
        stream.println(OutputCommand.UINPUT.format(gameProperties.size()));
        for (Entry<Object, Object> t : gameProperties.entrySet()) {
            stream.println(t.getKey() + "=" + t.getValue());
        }
    }

    public int getPlayerCount() {
        return players.size();
    }

    public List<T> getActivePlayers() {
        return players.stream().filter(p -> p.isActive()).collect(Collectors.toList());
    }

    public void execute(T player) {
        // SEND INPUTS FOR PLAYER

        InputCommand iCmd = InputCommand.parse(s.nextLine());

        if (iCmd.cmd != InputCommand.Command.GET_GAME_INFO) {
            throw new RuntimeException("Invalid command: " + iCmd.cmd);
        }

        frame++;
        dumpView();
        dumpInfos();
        dumpNextPlayerInput(player.getInputs().toArray(new String[0]));
        dumpNextPlayerInfos(player.getIndex(), player.getExpectedOutputLines(), getTurnMaxTime());

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

    public List<T> getPlayers() {
        return players;
    }

    public T getPlayer(int i) {
        return this.players.get(i);
    }

    public void endGame() {
        this.gameEnd = true;
    }

    public void setMaxTurns(int maxTurns) throws IllegalStateException, IllegalArgumentException {
        this.maxTurns = maxTurns;
    }

    public void setTurnMaxTime(int turnMaxTime) throws IllegalArgumentException {
        this.turnMaxTime = turnMaxTime;
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public int getTurnMaxTime() {
        return turnMaxTime;
    }

    public boolean isGameEnd() {
        return this.gameEnd;
    }
    
    public void setViewData(Object data) {
        setViewData("default", data);
    }
    
    public void setViewData(String moduleName, Object data) {
        this.currentViewData.add(moduleName, gson.toJsonTree(data));
    }

    public void setTooltips(List<Tooltip> tooltips) {
        this.currentTooltips = tooltips;
    }

    public void setGameSummary(List<String> gameSummary) {
        this.currentGameSummary = gameSummary;
    }

    void dumpView() {
        OutputData data = new OutputData(OutputCommand.VIEW);
        if (newTurn) {
            data.add(prevViewData.toString());
        } else {
            data.add(createNewView(false).toString());
        }
        System.err.println(data);
        out.println(data);
    }

    void dumpInfos() {
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

    void dumpNextPlayerInfos(int nextPlayer, int expectedOutputLineCount, int timeout) {
        OutputData data = new OutputData(OutputCommand.NEXT_PLAYER_INFO);
        data.add(String.valueOf(nextPlayer));
        data.add(String.valueOf(expectedOutputLineCount));
        data.add(String.valueOf(timeout));
        out.println(data);
    }

    void dumpNextPlayerInput(String[] input) {
        OutputData data = new OutputData(OutputCommand.NEXT_PLAYER_INPUT);
        data.addAll(input);
        out.println(data);
    }

    public static String getColoredReason(boolean error, String reason) {
        if (error) {
            return String.format("¤RED¤%s§RED§", reason);
        } else {
            return String.format("¤GREEN¤%s§GREEN§", reason);
        }
    }
}
