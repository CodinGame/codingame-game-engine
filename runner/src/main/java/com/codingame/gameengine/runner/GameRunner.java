package com.codingame.gameengine.runner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.codingame.gameengine.runner.Command.InputCommand;
import com.codingame.gameengine.runner.Command.OutputCommand;
import com.codingame.gameengine.runner.dto.AgentDto;
import com.codingame.gameengine.runner.dto.GameResultDto;
import com.codingame.gameengine.runner.dto.TooltipDto;
import com.codingame.gameengine.runner.simulate.AgentData;
import com.codingame.gameengine.runner.simulate.GameResult;
import com.codingame.gameengine.runner.simulate.TooltipData;
import com.google.gson.Gson;

abstract class GameRunner {

    static final String INTERRUPT_THREAD = "05&08#1981";
    private static final Pattern COMMAND_HEADER_PATTERN = Pattern
        .compile("\\[\\[(?<cmd>.+)\\] ?(?<lineCount>[0-9]+)\\]");

    protected static Log log = LogFactory.getLog(GameRunner.class);
    GameResultDto gameResult = new GameResultDto();
    private ByteArrayOutputStream refereeStdout;
    private ByteArrayOutputStream refereeStderr;

    private Agent referee;
    protected final List<Agent> players;
    private final List<AsynchronousWriter> writers = new ArrayList<>();
    private final List<BlockingQueue<String>> queues = new ArrayList<>();
    private boolean gameEnded = false;

    private String[] avatars = new String[] { "16085734516701", "16085846089817", "16085713250612", "16085756802960", "16085746254929",
        "16085763837151", "16085720641630", "16085834521247" };

    private static enum OutputResult {
        OK, TIMEOUT, TOOLONG, TOOSHORT
    };

    protected GameRunner() {
        referee = new RefereeAgent();
        players = new ArrayList<Agent>();
        refereeStdout = new ByteArrayOutputStream();
        refereeStderr = new ByteArrayOutputStream();
    }

    private void initialize(Properties conf) {
        if (players.size() == 0) throw new RuntimeException("You have to add at least one player");
        if (players.size() > 8) throw new RuntimeException("You may add up to eight players only");

        referee.initialize(conf);
        gameResult.outputs.put("referee", new ArrayList<>());
        gameResult.errors.put("referee", new ArrayList<>());

        for (int i = 0; i < players.size(); i++) {
            String id = String.valueOf(i);
            Agent player = players.get(i);
            player.initialize(conf);

            List<String> initOutputsValues = new ArrayList<>();
            initOutputsValues.add(null);
            gameResult.outputs.put(id, initOutputsValues);

            List<String> initErrorsValues = new ArrayList<>();
            gameResult.errors.put(id, initErrorsValues);

            AgentDto agent = new AgentDto();
            agent.index = i;
            agent.agentId = player.getAgentId();
            agent.avatar = player.getAvatar() != null ? player.getAvatar()
                : "https://static.codingame.com/servlet/fileservlet?id=" + avatars[i] + "&format=viewer_avatar";
            agent.name = player.getNickname() != null ? player.getNickname() : "Player " + i;
            gameResult.agents.add(agent);
        }
    }

    private void bootstrapPlayers() {
        boolean allFailed = true;
        for (int i = 0; i < players.size(); i++) {
            Agent player = players.get(i);
            player.execute();
            allFailed = allFailed && player.isFailed();
        }

        if (allFailed) {
            throw new RuntimeException("Bootstrap of all players failed to bootsrap");
        }

        for (Agent agent : players) {
            BlockingQueue<String> queue = new ArrayBlockingQueue<>(1024);
            AsynchronousWriter asyncWriter = new AsynchronousWriter(queue, agent.getInputStream());
            writers.add(asyncWriter);
            queues.add(queue);
            asyncWriter.start();
        }
    }

    private void runAgents() {
        referee.execute();

        bootstrapPlayers();

        readInitFrameErrors();

        Command initCommand = new Command(OutputCommand.INIT);
        initCommand.addLine(players.size());

        buildInitCommand(initCommand);

        referee.sendInput(initCommand.toString());
        int round = 0;
        while (true) {
            GameTurnInfo turnInfo = readGameInfo(round);
            boolean validTurn = turnInfo.isComplete();

            gameResult.failCause = turnInfo.get(InputCommand.FAIL).orElse(null);

            if (validTurn) {
                gameResult.outputs.get("referee").add(refereeStdout.toString());
                refereeStdout.reset();
                gameResult.summaries.add(turnInfo.get(InputCommand.SUMMARY).orElse(turnInfo.get(InputCommand.INFOS).orElse(null)));
            }

            if ((validTurn) && (!turnInfo.get(InputCommand.SCORES).isPresent())) {
                NextPlayerInfo nextPlayerInfo = new NextPlayerInfo(
                    turnInfo.get(InputCommand.NEXT_PLAYER_INFO).orElse(null)
                );
                String nextPlayerOutput = getNextPlayerOutput(
                    nextPlayerInfo,
                    turnInfo.get(InputCommand.NEXT_PLAYER_INPUT).orElse(null)
                );

                for (Agent a : players) {
                    gameResult.outputs.get(String.valueOf(a.getAgentId())).add(a.getAgentId() == nextPlayerInfo.nextPlayer ? nextPlayerOutput : null);
                }

                if (nextPlayerOutput != null) {
                    log.info("\t=== Read from player");
                    log.info(nextPlayerOutput);
                    log.info("\t=== End Player");
                    sendPlayerOutput(nextPlayerOutput, nextPlayerInfo.nbLinesNextOutput);
                } else {
                    sendTimeOut();
                }
            }

            readError(referee);
            if (!validTurn) {
                gameResult.views.add(null);
            } else {
                gameResult.views.add(turnInfo.get(InputCommand.VIEW).orElse(null));

                turnInfo.get(InputCommand.UINPUT).ifPresent(line -> {
                    gameResult.uinput.add(line);
                });

                turnInfo.get(InputCommand.METADATA).ifPresent(line -> {
                    gameResult.metadata = line;
                });

                final int currentRound = round;
                turnInfo.get(InputCommand.TOOLTIP).ifPresent(line -> {
                    String[] tooltipData = line.split("\n");
                    for (int i = 0; i < tooltipData.length / 2; ++i) {
                        String text = tooltipData[i * 2];
                        int eventId = Integer.valueOf(tooltipData[i * 2 + 1]);
                        gameResult.tooltips.add(new TooltipDto(text, eventId, currentRound));
                    }
                });

                turnInfo.get(InputCommand.SCORES).ifPresent(scores -> {
                    for (String line : scores.split("\n")) {
                        String[] parts = line.split(" ");
                        if (parts.length > 1) {
                            int player = Integer.decode(parts[0]);
                            int score = Integer.decode(parts[1]);
                            gameResult.scores.put(player, score);
                        }
                    }
                });
            }
            round++;
            if (!validTurn || turnInfo.isEndTurn()) {
                break;
            }
        }

        for (BlockingQueue<String> queue : queues) {
            queue.offer(INTERRUPT_THREAD);
        }

    }

    abstract protected void buildInitCommand(Command initCommand);

    private String getJSONResult() {
        addPlayerIds();

        return new Gson().toJson(gameResult);
    }

    private void addPlayerIds() {
        for (int i = 0; i < players.size(); i++) {
            gameResult.ids.put(i, players.get(i).getAgentId());
        }
    }

    /**
     * Read all output from standard error stream
     */
    private void readInitFrameErrors() {
        for (int i = 0; i < players.size(); i++) {
            Agent player = players.get(i);
            String id = String.valueOf(i);
            gameResult.errors.get(id).add(player.readError());
        }
    }

    /**
     * Read all output from standard error stream
     */
    private void readError(Agent agent) {
        if (agent == referee) {
            gameResult.errors.get("referee").add(refereeStderr.toString());
            refereeStderr.reset();
        } else {
            for (Agent a : players) {
                gameResult.errors.get(String.valueOf(a.getAgentId())).add(a == agent ? agent.readError() : null);
            }
        }
    }

    private void sendPlayerOutput(String output, int nbLines) {
        String[] lines = output.split("(\\n|\\r\\n)", -1);
        Command command = new Command(OutputCommand.SET_PLAYER_OUTPUT, Arrays.copyOfRange(lines, 0, nbLines));
        referee.sendInput(command.toString());
    }

    private void sendTimeOut() {
        Command command = new Command(OutputCommand.SET_PLAYER_TIMEOUT);
        referee.sendInput(command.toString());
    }

    private String getNextPlayerOutput(NextPlayerInfo nextPlayerInfo, String nextPlayerInput) {
        Agent player = players.get(nextPlayerInfo.nextPlayer);

        // Send player input to input queue
        queues.get(nextPlayerInfo.nextPlayer).offer(nextPlayerInput);

        // Wait for player output then read error
        String playerOutput = player.getOutput(nextPlayerInfo.nbLinesNextOutput, nextPlayerInfo.timeout);
        if (playerOutput != null)
            playerOutput = playerOutput.replace('\r', '\n');

        if (checkOutput(playerOutput, nextPlayerInfo.nbLinesNextOutput) == OutputResult.OK) {
            // Read this turn's stderr
            readError(player);
        } else {
            // Give the agent time to crash cleanly
            try {
                Thread.sleep(nextPlayerInfo.timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Read this turns stderr and the crash output
            readError(player);
            return null;
        }

        if ((playerOutput != null) && playerOutput.isEmpty() && (nextPlayerInfo.nbLinesNextOutput == 1)) {
            return "\n";
        }
        if (
            (playerOutput != null) && (playerOutput.length() > 0)
                && (playerOutput.charAt(playerOutput.length() - 1) != '\n')
        ) {
            return playerOutput + '\n';
        }
        return playerOutput;
    }

    private GameTurnInfo readGameInfo(int round) {
        GameTurnInfo turnInfo = new GameTurnInfo();

        referee.sendInput(new Command(OutputCommand.GET_GAME_INFO).toString());

        while (!turnInfo.isComplete() && !turnInfo.refereeHasFailed()) {
            Command command = readCommand(referee, round);
            if (command == null) {
                return turnInfo;
            }
            turnInfo.put(command);
        }
        return turnInfo;
    }

    private Command readCommand(Agent agent, int round) {
        try {
            String output = agent.getOutput(1, 150_000);
            if (output != null) {
                output = output.replace('\r', '\n');
            }
            if (checkOutput(output, 1) != OutputResult.OK) {
                throw new RuntimeException("Invalid Referee command: " + output);
            }

            Matcher m = COMMAND_HEADER_PATTERN.matcher(output.trim());
            if (m.matches()) {
                String command = m.group("cmd");
                int nbLinesToRead = Integer.parseInt(m.group("lineCount"));

                if (nbLinesToRead >= 0) {
                    output = agent.getOutput(nbLinesToRead, 150_000, round == 0);
                    output = output.replace('\r', '\n');
                } else {
                    throw new RuntimeException("Invalid Referee command line count: " + output);
                }
                if (checkOutput(output, nbLinesToRead) != OutputResult.OK) {
                    throw new RuntimeException(
                        "Error reading Referee command. Buffer capacity: " + output.length() + " / "
                            + (round == 0 ? RefereeAgent.REFEREE_MAX_BUFFER_SIZE_EXTRA : RefereeAgent.REFEREE_MAX_BUFFER_SIZE)
                    );
                }
                return new Command(InputCommand.valueOf(command), output);
            } else {
                throw new RuntimeException("Invalid referee command: " + output);
            }
        } catch (RuntimeException err) {
            err.printStackTrace();
            return new Command(InputCommand.FAIL, err.toString());
        }
    }

    private OutputResult checkOutput(String output, int nbExpectedLines) {
        if ((output == null) || (output.isEmpty())) {
            if (nbExpectedLines <= 0) {
                return OutputResult.OK;
            } else {
                return OutputResult.TIMEOUT;
            }
        }

        int nbOccurences = 0;
        for (int i = 0; i < output.length(); ++i) {
            if (output.charAt(i) == '\n') {
                ++nbOccurences;
            }
        }

        if (nbOccurences < nbExpectedLines) {
            return OutputResult.TOOSHORT;
        }
        if (nbOccurences > nbExpectedLines) {
            return OutputResult.TOOLONG;
        }
        return OutputResult.OK;
    }

    /**
     * Runs the game and attempts to start a server on the port 8888.
     * <p>
     * Open a webpage to the server to watch the game's replay.
     */
    public void start() {
        start(8888);
    }

    /**
     * Runs the game and attempts to start a server on the given port.
     * <p>
     * Open a webpage to the server to watch the game's replay.
     * 
     * @param port
     *            the port on which to attempt to start the a server for the game's replay.
     */
    public void start(int port) {
        runGame();

        new Renderer(port).render(players.size(), getJSONResult());
    }

    /**
     * Runs the game without a server and returns computed game results
     *
     * @return game result of the game
     */
    public GameResult simulate() {
        runGame();
        GameResult simulateResult = new GameResult();

        simulateResult.errors = gameResult.errors;
        simulateResult.outputs = gameResult.outputs;
        simulateResult.summaries = gameResult.summaries;
        simulateResult.views = gameResult.views;
        simulateResult.scores = gameResult.scores;
        simulateResult.gameParameters = gameResult.uinput;
        simulateResult.metadata = gameResult.metadata;
        simulateResult.tooltips = gameResult.tooltips.stream()
            .map(tooltipDto -> new TooltipData(tooltipDto.text, tooltipDto.event, tooltipDto.turn))
            .collect(Collectors.toList());
        simulateResult.agents = gameResult.agents.stream()
            .map(agentDto -> new AgentData(agentDto.index, agentDto.name, agentDto.avatar))
            .collect(Collectors.toList());
        simulateResult.failCause = gameResult.failCause;

        return simulateResult;
    }

    private void requireGameNotEnded() {
        if (gameEnded) {
            throw new RuntimeException("This game has ended");
        }
    }

    /**
     * Simulates the game and gathers game results
     */
    private void runGame() {
        PrintStream out = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                out.write(b);
                refereeStdout.write(b);
            }
        }));
        PrintStream err = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                err.write(b);
                refereeStderr.write(b);
            }
        }));
        requireGameNotEnded();
        Properties conf = new Properties();
        initialize(conf);

        runAgents();

        referee.destroy();
        destroyPlayers();
        gameEnded = true;
        System.setOut(out);
        System.setErr(err);
    }

    /**
     * Destroys all players
     */
    private void destroyPlayers() {
        for (Agent player : players) {
            player.destroy();
        }
    }

    static class NextPlayerInfo {

        int nextPlayer;
        int nbLinesNextOutput;
        long timeout;

        NextPlayerInfo(String command) {
            String[] nextPlayerInfo = command.split("\n");
            nextPlayer = Integer.decode(nextPlayerInfo[0]);
            nbLinesNextOutput = Integer.decode(nextPlayerInfo[1]);
            timeout = Long.decode(nextPlayerInfo[2]);
        }
    }
}
