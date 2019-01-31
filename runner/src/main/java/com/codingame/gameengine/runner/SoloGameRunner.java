package com.codingame.gameengine.runner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The class to use to run local games and display the replay in a webpage on a temporary local server.
 */
public class SoloGameRunner extends GameRunner {

    private List<String> testCaseInput;

    /**
     * Once this runner is instantiated, the system property game.mode is set to "solo"
     */
    public SoloGameRunner() {
        System.setProperty("game.mode", "solo");
    }

    private List<String> getLinesFromTestCaseFile(File file) {
        List<String> lines = new ArrayList<>();
        try {
            JsonObject testCaseJson = new JsonParser().parse(FileUtils.readFileToString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            lines.addAll(Arrays.asList(testCaseJson.get("testIn").getAsString().split("\\n")));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read file", e);
        } catch (NullPointerException e) {
            throw new RuntimeException("Cannot find \"testIn\" property");
        } catch (Exception e) {
            throw new RuntimeException("Cannot parse file", e);
        }
        return lines;
    }

    /**
     * Sets a test case file by file path which <b>testIn</b> value will be sent to the Game Manager as a test case input.
     * <p>
     * The file path must be relative considering the root directory is <b>config</b>.
     * 
     * @param testCaseFileName
     *            the test case file path
     */
    public void setTestCase(String testCaseFileName) {
        setTestCase(new File(System.getProperty("user.dir")).toPath().resolve("config/" + testCaseFileName).toFile());
    }

    /**
     * Sets a test case file which <b>testIn</b> value will be sent to the Game Manager as a test case input.
     * 
     * @param testCaseFile
     *            the test case file
     */
    public void setTestCase(File testCaseFile) {
        if (testCaseFile == null) {
            throw new RuntimeException("Given test case is not a file.");
        }
        if (!testCaseFile.isFile()) {
            throw new RuntimeException("Given test case is not a file " + testCaseFile.getAbsolutePath());
        }
        
        setTestCaseInput(getLinesFromTestCaseFile(testCaseFile));
    }
    
    /**
     * Sets a list of <code>String</code> as a test case input that will be sent to the Game Manager.
     * 
     * @param testCaseInput
     *            the list of <code>String</code> of the test case input
     */
    public void setTestCaseInput(List<String> testCaseInput) {
        this.testCaseInput = testCaseInput;
    }
    
    /**
     * Sets a <code>String</code> as a test case input that will be sent to the Game Manager.
     * <p>
     * The input will be split in several lines by <b>&#92;n</b>.
     * 
     * @param testCaseInput
     *            the <code>String</code> of the test case input
     */
    public void setTestCaseInput(String testCaseInput) {
        setTestCaseInput(Arrays.asList(testCaseInput.split("\\n")));
    }

    private void setAgent(Agent player, String nickname, String avatar) {
        if (!players.isEmpty()) {
            players.clear();
        }
        player.setAgentId(0);
        player.setNickname(nickname);
        player.setAvatar(avatar);
        players.add(player);
    }

    /**
     * Sets an AI to the next game to run.
     * 
     * @param playerClass
     *            the Java class of an AI for your game.
     */
    public void setAgent(Class<?> playerClass) {
        setAgent(new JavaPlayerAgent(playerClass.getName()), null, null);
    }

    /**
     * Sets an AI to the next game to run.
     * <p>
     * The given command will be executed with <code>Runtime.getRuntime().exec()</code>.
     * 
     * @param commandLine
     *            the system command line to run the AI.
     */
    public void setAgent(String commandLine) {
        setAgent(new CommandLinePlayerAgent(commandLine), null, null);
    }

    /**
     * Sets an AI to the next game to run.
     * 
     * @param playerClass
     *            the Java class of an AI for your game.
     * @param nickname
     *            the player's nickname
     * @param avatarUrl
     *            the url of the player's avatar
     */
    public void setAgent(Class<?> playerClass, String nickname, String avatarUrl) {
        setAgent(new JavaPlayerAgent(playerClass.getName()), nickname, avatarUrl);
    }

    /**
     * Sets an AI to the next game to run.
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
    public void setAgent(String commandLine, String nickname, String avatarUrl) {
        setAgent(new CommandLinePlayerAgent(commandLine), nickname, avatarUrl);
    }

    @Override
    protected void buildInitCommand(Command initCommand) {
        if (testCaseInput != null && !testCaseInput.isEmpty()) {
            for (String line : testCaseInput) {
                initCommand.addLine(line);
            }
        } else {
            throw new RuntimeException("Test case was not set. A solo game needs a test case to run.");
        }
    }

}
