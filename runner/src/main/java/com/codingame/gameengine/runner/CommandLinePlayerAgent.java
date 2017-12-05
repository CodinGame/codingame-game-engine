package com.codingame.gameengine.runner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Properties;

public class CommandLinePlayerAgent extends Agent {

	private PipedInputStream agentStdin = new PipedInputStream(100000);
	private PipedOutputStream agentStdout = new PipedOutputStream();
	private PipedOutputStream agentStderr = new PipedOutputStream();

	private OutputStream processStdin;
	private InputStream processStdout;
	private InputStream processStderr;
    private String commandLine;

    public CommandLinePlayerAgent(String commandLine) {
		super();

		this.commandLine = commandLine;

		try {
			processStdin = new PipedOutputStream(agentStdin);
			processStdout = new PipedInputStream(agentStdout, 100000);
			processStderr = new PipedInputStream(agentStderr, 100000);
		} catch(IOException e) {
			throw new RuntimeException("Cannot initialize Player Agent", e);
		}
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
    }

    @Override
    public String getOutput(int nbLine, long timeout) {
        String output = super.getOutput(nbLine, timeout);
        System.out.println("\t=== Read from player");
        System.out.print(output);
        System.out.println("\t=== End Player");
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
        PipedInputStream agentStdin = new PipedInputStream(100000);
        processStdin = new PipedOutputStream(agentStdin);

        agentStdout = new PipedOutputStream();
        processStdout = new PipedInputStream(agentStdout, 100000);
        agentStderr = new PipedOutputStream();
        processStderr = new PipedInputStream(agentStderr, 100000);
    }

    @Override
    public void destroy() {
    }
}

