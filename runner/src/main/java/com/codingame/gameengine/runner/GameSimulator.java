package com.codingame.gameengine.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.codingame.gameengine.runner.dto.GameResult;
import com.codingame.gameengine.runner.dto.Tooltip;
import com.google.gson.Gson;

public class GameSimulator {

	public static final String INTERRUPT_THREAD = "05&08#1981";
	private static final Pattern COMMAND_HEADER_PATTERN = Pattern
			.compile("\\[\\[(?<cmd>.+)\\] ?(?<lineCount>[0-9]+)\\]");

	protected static Log log = LogFactory.getLog(GameSimulator.class);
	GameResult gameResult = new GameResult();

	private Agent referee = null;
	private final List<Agent> players = new ArrayList<Agent>();
	private final List<AsynchronousWriter> writers = new ArrayList<>();
	private final List<BlockingQueue<String>> queues = new ArrayList<>();

	private static enum OutputResult {
		OK, TIMEOUT, TOOLONG, TOOSHORT
	};

	public GameSimulator() {
	}

	public void initialize(Properties conf) {
		if(players.size() == 0) throw new RuntimeException("You have to add at least one player");
		
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
//			initErrorsValues.add(null);
			gameResult.errors.put(id, initErrorsValues);
		}
	}
	
	private void bootstrapPlayers() {
		boolean allFailed = true;
		for (int i = 0; i < players.size(); i++) {
			Agent player = players.get(i);
			player.execute();
//			if (player.isFailed()) {
//				gameResult.errors.get(String.valueOf(i)).add(player.getResult());
//			}
			allFailed = allFailed && player.isFailed();
		}

		if (allFailed) {
			throw new RuntimeException("Bootstrap of all players failed to bootsrap");
		}

		try {
			Thread.sleep(300); // Arbitrary time to wait for bootstrap
		} catch (InterruptedException e) {
		}

		for (Agent agent : players) {
			BlockingQueue<String> queue = new ArrayBlockingQueue<>(1024);
			AsynchronousWriter asyncWriter = new AsynchronousWriter(queue, agent.getInputStream());
			writers.add(asyncWriter);
			queues.add(queue);
			asyncWriter.start();
		}
	}

	public void run() {
		referee.execute();

		bootstrapPlayers();

		readInitFrameErrors();

		// Sends init (1 line by default, multiple lines if init data available)
		int nbLine = 1;
		StringBuilder initBuilder = new StringBuilder();
		if (getRefereeInput() != null) {
			try (Scanner scanner = new Scanner(getRefereeInput())) {
				while (scanner.hasNextLine()) {
					initBuilder.append(scanner.nextLine());
					initBuilder.append("\n");
					nbLine++;
				}
			}
		}
		String initLine = String.format("[[INIT] %d]\n%d\n", nbLine, players.size());
		initBuilder.insert(0, initLine);

		referee.sendInput(initBuilder.toString());
		int round = 0;
		while (true) {
			Map<String, String> commands = readGameInfo();

			if (commands != null) {
				gameResult.outputs.get("referee").add(commands.get("INFOS"));
				gameResult.summaries.add(commands.get("SUMMARY"));
			}

			if ((commands != null) && (!commands.containsKey("SCORES"))) {
				NextPlayerInfo nextPlayerInfo = new NextPlayerInfo(commands.get("NEXT_PLAYER_INFO"));
				String nextPlayerOutput = getNextPlayerOutput(nextPlayerInfo, commands.get("NEXT_PLAYER_INPUT"));
				if (nextPlayerOutput != null) {
					sendPlayerOutput(nextPlayerOutput, nextPlayerInfo.nbLinesNextOutput);
				} else {
					sendTimeOut();
				}
			}

			readError(referee);
			if (commands == null) {
				gameResult.views.add(null);
			} else {
				gameResult.views.add(commands.get("VIEW"));
			}

			if ((commands != null) && commands.containsKey("UINPUT")) {
				gameResult.uinput.add(commands.get("UINPUT"));
			}

			if ((commands != null) && commands.containsKey("METADATA")) {
				gameResult.metadata = commands.get("METADATA");
			}

			if ((commands != null) && commands.containsKey("TOOLTIP")) {
				String[] tooltipData = commands.get("TOOLTIP").split("\n");
				for (int i = 0; i < tooltipData.length / 2; ++i) {
					String text = tooltipData[i * 2];
					int eventId = Integer.valueOf(tooltipData[i * 2 + 1]);
					gameResult.tooltips.add(new Tooltip(text, eventId, round));
				}
			}

			if ((commands != null) && commands.containsKey("SCORES")) {
				String scores = commands.get("SCORES");
				for (String line : scores.split("\n")) {
					String[] parts = line.split(" ");
					if (parts.length > 1) {
						int player = Integer.decode(parts[0]);
						int score = Integer.decode(parts[1]);
						gameResult.scores.put(player, score);
					}
				}
			}
			round++;
			if ((commands == null) || commands.containsKey("SCORES")) {
				break;
			}
		}

		for (BlockingQueue<String> queue : queues) {
			queue.offer(INTERRUPT_THREAD);
		}

	}

	public String getJSONResult() {
		for (int i = 0; i < players.size(); i++) {
			gameResult.ids.put(i, players.get(i).getAgentId());
		}
		return new Gson().toJson(gameResult);
	}

	/**
	 * Read all output from standard error stream
	 */
	private void readInitFrameErrors() {
		gameResult.errors.get("referee").add(referee.readError());
		for (int i = 0; i < players.size(); i++) {
			Agent player = players.get(i);
			String id = String.valueOf(i);
//			String err = player.readError();
//			if (err != null) {
				gameResult.errors.get(id).add(player.readError());
//				String newError = gameResult.errors.get(id).get(0) + err;
//				gameResult.errors.get(id).set(0, newError);
//			}
		}
	}

	/**
	 * Read all output from standard error stream
	 */
	private void readError(Agent agent) {
		if(agent == referee) {
			gameResult.errors.get("referee").add(referee.readError());
		} else {
			for (int i = 0; i < players.size(); i++) {
				if (players.get(i) == agent) {
					gameResult.errors.get(String.valueOf(i)).add(agent.readError());
					break;
				}
			}
		}
	}

	private void sendPlayerOutput(String output, int nbLines) {
		StringBuilder toSend = new StringBuilder();
		toSend.append("[[SET_PLAYER_OUTPUT] ");
		toSend.append(nbLines);
		toSend.append("]\n");
		toSend.append(output);
		referee.sendInput(toSend.toString());
	}

	private void sendTimeOut() {
		referee.sendInput("[[SET_PLAYER_TIMEOUT] 0]\n");
	}

	private String getNextPlayerOutput(NextPlayerInfo nextPlayerInfo, String nextPlayerInput) {
		Agent player = players.get(nextPlayerInfo.nextPlayer);

		// Send player input to input queue
		queues.get(nextPlayerInfo.nextPlayer).offer(nextPlayerInput);

		// Wait for player output then read error
		String playerOutput = player.getOutput(nextPlayerInfo.nbLinesNextOutput, nextPlayerInfo.timeout);
		if (playerOutput != null)
			playerOutput = playerOutput.replace('\r', '\n');
		readError(player);

		gameResult.outputs.get(String.valueOf(nextPlayerInfo.nextPlayer)).add(playerOutput);

		if (checkOutput(playerOutput, nextPlayerInfo.nbLinesNextOutput) != OutputResult.OK)
			return null;
		if ((playerOutput != null) && playerOutput.isEmpty() && (nextPlayerInfo.nbLinesNextOutput == 1))
			return "\n";
		if ((playerOutput != null) && (playerOutput.length() > 0)
				&& (playerOutput.charAt(playerOutput.length() - 1) != '\n'))
			return playerOutput + '\n';

		return playerOutput;
	}

	private Map<String, String> readGameInfo() {
		Map<String, String> commands = new HashMap<String, String>();
		referee.sendInput("[[GET_GAME_INFO] 0]\n");
		while ((!commands.containsKey("NEXT_PLAYER_INPUT") || !commands.containsKey("VIEW")
				|| !commands.containsKey("NEXT_PLAYER_INFO") || !commands.containsKey("INFOS"))
				&& (!commands.containsKey("VIEW") || !commands.containsKey("SCORES")
						|| !commands.containsKey("INFOS"))) {
			String[] command = readCommand(referee);
			if (command == null)
				return null;
			commands.put(command[0], command[1]);
		}
		return commands;
	}

	private String[] readCommand(Agent agent) {
		String output = "";
		output = agent.getOutput(1, 1500);
		if (output != null)
			output = output.replace('\r', '\n');
		if (checkOutput(output, 1) != OutputResult.OK)
			return null;

		Matcher m = COMMAND_HEADER_PATTERN.matcher(output.trim());
		if (m.matches()) {
			String command = m.group("cmd");
			int nbLinesToRead = Integer.parseInt(m.group("lineCount"));

			if (nbLinesToRead >= 0) {
				output = agent.getOutput(nbLinesToRead, 500);
			} else {
				output = null;
			}
			if (checkOutput(output, nbLinesToRead) != OutputResult.OK)
				return null;
			return new String[] { command, output };
		} else {
			throw new RuntimeException("Invalid referee command: " + output);
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

		if (nbOccurences < nbExpectedLines)
			return OutputResult.TOOSHORT;
		if (nbOccurences > nbExpectedLines)
			return OutputResult.TOOLONG;
		return OutputResult.OK;
	}

	public void addPlayer(Agent player) {
		players.add(player);
	}

	public List<Agent> getPlayers() {
		return players;
	}

	public void setReferee(Agent referee) {
		this.referee = referee;
	}

	public String getRefereeInput() {
		return gameResult.refereeInput;
	}

	public void setRefereeInput(String refereeInput) {
		gameResult.refereeInput = refereeInput;
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