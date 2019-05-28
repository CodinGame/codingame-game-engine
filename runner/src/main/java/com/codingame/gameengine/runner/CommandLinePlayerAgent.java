package com.codingame.gameengine.runner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * This class is used in with the <code>GameRunner</code> to add an AI as a player.
 */
public class CommandLinePlayerAgent extends Agent {

    private OutputStream processStdin;
    private InputStream processStdout;
    private InputStream processStderr;
    private String[] commandArray;
    private Process process;

    /**
     * Creates an Agent for your game, will run the given commandLine at game start
     * 
     * @param commandLine
     *            the command line to run
     */
    public CommandLinePlayerAgent(String commandLine) {
        super();
        this.commandArray = commandLine.split(" ");
    }
    
    /**
     * Creates an Agent for your game, will run the given commandLine at game start
     * 
     * @param commandArray
     *            the command array to run
     */
    public CommandLinePlayerAgent(String[] commandArray) {
        super();
        this.commandArray = commandArray;
    }

    @Override
    protected OutputStream getInputStream() {
        return processStdin;
    }

    @Override
    protected InputStream getOutputStream() {
        return processStdout;
    }

    @Override
    protected InputStream getErrorStream() {
        return processStderr;
    }

    @Override
    public void initialize(Properties conf) {
        try {
            this.process = Runtime.getRuntime().exec(commandArray);
        } catch (IOException e) {
            throw new RuntimeException("Failed to launch " + String.join(" ", commandArray));
        }
        processStdin = process.getOutputStream();
        processStdout = process.getInputStream();
        processStderr = process.getErrorStream();
    }

    @Override
    public String getOutput(int nbLine, long timeout) {
        String output = super.getOutput(nbLine, timeout);
        return output;
    }

    /**
     * Launch the agent. After the call, agent is ready to process input / output
     * 
     * @throws Exception
     *             if an error occurs
     */

    @Override
    protected void runInputOutput() throws Exception {

    }

    @Override
    public void destroy() {
        process.destroy();
    }
}
