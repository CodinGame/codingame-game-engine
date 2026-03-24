package com.codingame.gameengine.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import com.codingame.gameengine.core.RefereeMain;

class RefereeAgent extends Agent {

    public static final int REFEREE_MAX_BUFFER_SIZE_EXTRA = 100_000;
    public static final int REFEREE_MAX_BUFFER_SIZE = 30_000;

    private PipedInputStream agentStdin = new PipedInputStream(100_000);
    private PipedOutputStream agentStdout = new PipedOutputStream();
    private PipedOutputStream agentStderr = new PipedOutputStream();

    private OutputStream processStdin = null;
    private InputStream processStdout = null;
    private BufferedReader processStdoutReader;
    private InputStream processStderr = null;

    private Thread thread;

    public RefereeAgent() {
        super();

        try {
            processStdin = new PipedOutputStream(agentStdin);
            processStdout = new PipedInputStream(agentStdout, 100_000);
            processStdoutReader = new BufferedReader(new InputStreamReader(processStdout, UTF8));
            processStderr = new PipedInputStream(agentStderr, 100_000);
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize Referee Agent");
        }
    }

    @Override
    public void destroy() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    protected OutputStream getInputStream() {
        return processStdin;
    }

    @Override
    protected BufferedReader getOutputReader() {
        return processStdoutReader;
    }

    @Override
    protected InputStream getErrorStream() {
        return processStderr;
    }

    @Override
    protected void runInputOutput() throws Exception {

        thread = new Thread() {
            public void run() {
                RefereeMain.start(agentStdin, new PrintStream(agentStdout));
            }
        };
        thread.start();
    }

    @Override
    public String getOutput(int nbLine, long timeout) {
        return getOutput(nbLine, timeout, false);
    }

    @Override
    public String getOutput(int nbLine, long timeout, boolean extraBufferSpace) {
        int limit = extraBufferSpace ? REFEREE_MAX_BUFFER_SIZE_EXTRA : REFEREE_MAX_BUFFER_SIZE;
        return getOutput(nbLine, timeout, limit);
    }
}