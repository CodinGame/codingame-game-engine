package com.codingame.gameengine.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract class Agent {

    public static final Charset UTF8 = Charset.forName("UTF-8");
    public static final int AGENT_MAX_BUFFER_SIZE = 10_000;
    public static final int THRESHOLD_LIMIT_STDERR_SIZE = 4096 * 50;

    /**
     * Single dedicated thread for all blocking agent I/O — sequential game loop
     * never needs more than one.
     */
    private static final ExecutorService AGENT_IO_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "agent-reader");
        t.setDaemon(true);
        return t;
    });

    private static Log log = LogFactory.getLog(Agent.class);

    private OutputStream processStdin;
    private BufferedReader processStdoutReader;
    private InputStream processStderr;
    private int totalStderrBytesSent = 0;
    private int agentId;
    private boolean failed = false;

    private String nickname;
    private String avatar;

    public long lastExecutionTimeMs;

    public Agent() {
    }

    abstract protected OutputStream getInputStream();

    abstract protected BufferedReader getOutputReader();

    abstract protected InputStream getErrorStream();

    /**
     * Initialize an agent given global properties. A call to this function is
     * needed before-all
     *
     * @param conf
     *             Global configuration
     */
    public void initialize(Properties conf) {
        this.lastExecutionTimeMs = 0;
    }

    /**
     * Compile and run an agent. After this, agent is ready for input / output
     */
    public void execute() {
        try {
            this.processStdin = getInputStream();
            this.processStdoutReader = getOutputReader();
            this.processStderr = getErrorStream();
            runInputOutput();
        } catch (Exception e) {
            setFailed(true);
            log.error("" + e.getMessage(), e);
        }
    }

    public void destroy() {
    }

    /**
     * Launch the agent. After the call, agent is ready to process input / output
     *
     * @throws Exception
     *                   if an error occurs
     */
    protected abstract void runInputOutput() throws Exception;

    /**
     * Write 'input' to standard input of agent
     *
     * @param input
     *              an input to write
     */
    public void sendInput(String input) {
        if (processStdin != null) {
            try {
                if (log.isTraceEnabled()) {
                    log.trace("Send input to agent " + this.agentId + " : " + input);
                }
                processStdin.write(input.getBytes(UTF8));
                processStdin.flush();
            } catch (IOException e) {
                processStdin = null;
            }
        }
    }

    /**
     * Get the output of an agent
     *
     * @param nbLine
     *                Number of lines wanted
     * @param timeout
     *                Stop reading after timeout milliseconds
     * @return the agent output, or null if timed out or reader closed
     */
    public String getOutput(int nbLine, long timeout) {
        return getOutput(nbLine, timeout, AGENT_MAX_BUFFER_SIZE);
    }

    /**
     * Read at most maxOutputSize bytes across nbLine lines.
     * 
     * @param nbLine
     *                      Number of lines wanted
     * @param timeout
     *                      Stop reading after timeout milliseconds
     * @param maxOutputSize
     *                      Maximum number of bytes to read
     * @return the agent output, or null if timed out or reader closed
     */
    protected final String getOutput(int nbLine, long timeout, int maxOutputSize) {
        if (processStdoutReader == null) {
            return null;
        }

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < nbLine; i++) {
                    String line = processStdoutReader.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line).append('\n');
                    if (sb.length() >= maxOutputSize) {
                        break;
                    }
                }
                return sb.toString();
            } catch (IOException e) {
                return null;
            }
        }, AGENT_IO_EXECUTOR);

        try {
            return future.orTimeout(timeout, TimeUnit.MILLISECONDS).join();
        } catch (CompletionException e) {
            future.cancel(true);
            return null;
        }
    }

    /**
     * Read all errors from standard error stream
     *
     * @return all errors
     */
    public String readError() {
        if (processStderr == null) {
            return null;
        }
        try {
            if (processStderr.available() > 0) {
                int limitStderrSize = 4096;
                if (totalStderrBytesSent > THRESHOLD_LIMIT_STDERR_SIZE) {
                    limitStderrSize = 1024;
                }

                byte[] tmp = new byte[limitStderrSize];
                int nbRead = processStderr.read(tmp, 0, limitStderrSize);
                return new String(tmp, 0, nbRead, UTF8);

            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean isFailed() {
        return this.failed;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getOutput(int nbLine, long timeout, boolean extraBufferSpace) {
        return getOutput(nbLine, timeout);
    }
}