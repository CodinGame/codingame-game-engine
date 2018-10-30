package com.codingame.gameengine.runner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import com.codingame.gameengine.core.RefereeMain;

class RefereeAgent extends Agent {

    public static final int REFEREE_MAX_BUFFER_SIZE_EXTRA = 100_000;
    public static final int REFEREE_MAX_BUFFER_SIZE = 30_000;
    private boolean lastRefereeByteIsCarriageReturn = false;

    private PipedInputStream agentStdin = new PipedInputStream(100_000);
    private PipedOutputStream agentStdout = new PipedOutputStream();
    private PipedOutputStream agentStderr = new PipedOutputStream();

    private OutputStream processStdin = null;
    private InputStream processStdout = null;
    private InputStream processStderr = null;
    
    private Thread thread;

    public RefereeAgent() {
        super();

        try {
            processStdin = new PipedOutputStream(agentStdin);
            processStdout = new PipedInputStream(agentStdout, 100_000);
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
    protected InputStream getOutputStream() {
        return processStdout;
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
        if (processStdout == null) {
            return null;
        }
        int maxBufferSize = extraBufferSpace ? REFEREE_MAX_BUFFER_SIZE_EXTRA : REFEREE_MAX_BUFFER_SIZE;
        try {
            byte[] tmp = new byte[maxBufferSize];
            int offset = 0;
            int nbOccurences = 0;

            long t0 = System.nanoTime();

            while ((offset < maxBufferSize) && (nbOccurences < nbLine)) {
                long current = System.nanoTime();
                if ((current - t0) > (timeout * 1_000_000L)) {
                    break;
                }

                while ((offset < maxBufferSize) && (processStdout.available() > 0)
                        && (nbOccurences < nbLine)) {
                    current = System.nanoTime();
                    if ((current - t0) > (timeout * 1_000_000L)) {
                        break;
                    }

                    int nbRead = processStdout.read(tmp, offset, 1);
                    if (nbRead <= 0) {
                        break;
                    }
                    byte curByte = tmp[offset];
                    if (!((curByte == '\n') && lastRefereeByteIsCarriageReturn)) {
                        offset += nbRead;
                        if ((curByte == '\n') || (curByte == '\r')) {
                            ++nbOccurences;
                        }
                    }
                    lastRefereeByteIsCarriageReturn = curByte == '\r';
                }

                if (!((offset < REFEREE_MAX_BUFFER_SIZE) && (nbOccurences < nbLine))) {
                    break;
                }

                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                }
            }
            return new String(tmp, 0, offset, UTF8);
        } catch (IOException e) {
            e.printStackTrace();
            processStdout = null;
        }
        return null;
    }
}