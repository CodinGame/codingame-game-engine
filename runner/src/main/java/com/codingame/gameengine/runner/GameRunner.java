package com.codingame.gameengine.runner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import fi.iki.elonen.SimpleWebServer;

public class GameRunner {
	public static final Charset UTF8 = Charset.forName("UTF-8");

	private GameSimulator gameSimulator;
	private Agent referee;
	private List<Agent> players;

	public GameRunner() {
		try {
			players = new ArrayList<>();
			gameSimulator = new GameSimulator();
			referee = new RefereeAgent();
			gameSimulator.setReferee(referee);

			File paramsFile = new File("params.txt");
			if (paramsFile != null && paramsFile.exists()) {
				String params = FileUtils.readFileToString(paramsFile, UTF8);
				gameSimulator.setRefereeInput(params);
			}
		} catch (IOException e) {
			throw new RuntimeException("Cannot initialize game", e);
		}
	}

	public void addJavaPlayer(Class<?> playerClass) {
		players.add(new JavaPlayerAgent(playerClass.getName()));
	}

	public void addExternalPlayer(String commandLine) {
	    players.add(new CommandLinePlayerAgent(commandLine));
	}

	public void start() {
		int id = 0;
		for (Agent agent : players) {
			agent.setAgentId(++id);
			gameSimulator.addPlayer(agent);
		}

		// Launch game
		Properties conf = new Properties();
		gameSimulator.initialize(conf);
		gameSimulator.run();

		Path path = generateView(gameSimulator);
		serveHTTP(path, 8080);
	}

	private void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) { // some JVMs return null for empty directories
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

	private Path generateView(GameSimulator gameSimulator) {
		String result = gameSimulator.getJSONResult();
		System.err.println(result);
		Path tmpdir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("codingame");
		deleteFolder(tmpdir.toFile());
		tmpdir.toFile().mkdirs();

		File temp = tmpdir.resolve("test.html").toFile();

		try (PrintWriter writer = new PrintWriter(new FileWriter(temp))) {
			FileUtils.copyDirectory(new File(GameRunner.class.getResource("/view").getFile()), tmpdir.toFile());

			File[] listFiles = tmpdir.resolve("js").toFile().listFiles();
			Arrays.sort(listFiles);
			String scripts = Stream.of(listFiles)
					.map(file -> "<script type='module' src='js/" + file.getName() + "'></script>")
					.collect(Collectors.joining("\n"));
			for (String f : IOUtils.readLines(GameRunner.class.getResourceAsStream("/view/test_tpl.html"))) {
				f = f.replace("[DATA]", result);
				f = f.replace("[PLAYERCOUNT]", String.valueOf(gameSimulator.getPlayers().size()));
				f = f.replace("[VIEWERJS]", scripts);
				writer.println(f);
			}
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Cannot generate the html file", e);
		}

		return tmpdir;
	}

	private void serveHTTP(Path path, int port) {
		System.out.println("http://localhost:" + port + "/test.html");
		System.out.println("Web server dir 1 : " + path.toAbsolutePath().toString());
		SimpleWebServer.main(("--quiet --port " + port + " --dir " + path.toAbsolutePath()).split(" "));
	}
}
