package com.codingame.gameengine.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SoloGameRunner extends GameRunner {
    
    private List<String> testCaseContent;

    private List<String> getLines(File file) {
        List<String> lines = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            String content = null;
            while ((content = bufferedReader.readLine()) != null) {
                lines.add(content);
            }
            bufferedReader.close();
        } catch (IOException e) {
            throw new RuntimeException("Cannot read file");
        }
        return lines;
    }
    
    public void setTestCase(String testCaseFileName) {
        setTestCase(new File(System.getProperty("user.dir")).toPath().resolve("config/" + testCaseFileName).toFile());
    }
    
    public void setTestCase(File testCaseFile) {
        if (testCaseFile != null && testCaseFile.isFile()) {
            testCaseContent = getLines(testCaseFile);
            //Removes title 
            testCaseContent.remove(0);
        } else {
            throw new RuntimeException("Given test case is not a file.");
        }
    }
    
    private void setAgent(Agent player, String nickname, String avatar) {
        if(!players.isEmpty()) {
            players.clear();
        }
        player.setAgentId(0);
        player.setNickname(nickname);
        player.setAvatar(avatar);
        players.add(player);
    }
    
    /**
     * @deprecated Sets an AI to the next game to run.
     *             <p>
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
     * @deprecated Sets an AI to the next game to run.
     *             <p>
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
    protected void setCommandInput(Command initCommand) {
        if (testCaseContent != null && !testCaseContent.isEmpty()) {
            for (String line : testCaseContent) {
                initCommand.addLine(line);
            }
        } else {
            throw new RuntimeException("Test case was not set. A solo game needs a test case to run.");
        }
    }

}
