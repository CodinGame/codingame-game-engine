package com.codingame.gameengine.runner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract class Agent {

    public static final Charset UTF8 = Charset.forName("UTF-8");
    public static final int AGENT_MAX_BUFFER_SIZE = 10_000;
    public static final int THRESHOLD_LIMIT_STDERR_SIZE = 4096 * 50;

    private static Log log = LogFactory.getLog(Agent.class);

    private OutputStream processStdin;
    private InputStream processStdout;
    private InputStream processStderr;
    private int totalStderrBytesSent = 0;
    private int agentId;
    private boolean lastAgentByteIsCarriageReturn = false;
    private boolean failed = false;

    private String nickname;
    private String avatar;

    public Agent() {
    }

    abstract protected OutputStream getInputStream();

    abstract protected InputStream getOutputStream();

    abstract protected InputStream getErrorStream();

    /**
     * Initialize an agent given global properties. A call to this function is needed before-all
     *
     * @param conf
     *            Global configuration
     */
    public void initialize(Properties conf) {
    }

    /**
     * Compile and run an agent. After this, agent is ready for input / output
     */
    public void execute() {
        try {
            this.processStdin = getInputStream();
            this.processStdout = getOutputStream();
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
     *             if an error occurs
     */
    protected abstract void runInputOutput() throws Exception;

    /**
     * Write 'input' to standard input of agent
     *
     * @param input
     *            an input to write
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
     *            Number of lines wanted
     * @param timeout
     *            Stop reading after timeout milliseconds
     * @return the agent output
     */
    public String getOutput(int nbLine, long timeout) {
        if (processStdout == null) {
            return null;
        }

        try {
            byte[] tmp = new byte[AGENT_MAX_BUFFER_SIZE];
            int offset = 0;
            int nbOccurences = 0;

            long t0 = System.nanoTime();

            while ((offset < AGENT_MAX_BUFFER_SIZE) && (nbOccurences < nbLine)) {
                long current = System.nanoTime();
                if ((current - t0) > (timeout * 1_000_000l)) {
                    break;
                }

                if (processStdout.available() > 0) {
                    int nbRead = processStdout.read(tmp, offset, 1);
                    if (nbRead < 0) {
                        // Should not happen, just in case...
                        break;
                    }
                    byte curByte = tmp[offset];
                    if (!((curByte == '\n') && lastAgentByteIsCarriageReturn)) {
                        offset += nbRead;
                        if ((curByte == '\n') || (curByte == '\r')) {
                            ++nbOccurences;
                        }
                    }
                    lastAgentByteIsCarriageReturn = curByte == '\r';
                } else {
                    if ((offset < AGENT_MAX_BUFFER_SIZE) && (nbOccurences < nbLine)) {
                        Thread.sleep(1);
                    }
                }
            }

            return new String(tmp, 0, offset, UTF8);
        } catch (IOException e1) {
            processStdout = null;
        } catch (InterruptedException e) {
            // wtf
        }
        return null;
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