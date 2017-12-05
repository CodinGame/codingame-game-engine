package com.codingame.gameengine.runner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import com.codingame.gameengine.core.RefereeMain;

public class RefereeAgent extends Agent {

	public static final int REFEREE_MAX_BUFFER_SIZE = 30000;
	private boolean lastRefereeByteIsCarriageReturn = false;

	private PipedInputStream agentStdin = new PipedInputStream(100000);
	private PipedOutputStream agentStdout = new PipedOutputStream();
	private PipedOutputStream agentStderr = new PipedOutputStream();

	private OutputStream processStdin = null;
	private InputStream processStdout = null;
	private InputStream processStderr = null;
	
	public RefereeAgent() {
		super();

		try {
			processStdin = new PipedOutputStream(agentStdin);
			processStdout = new PipedInputStream(agentStdout, 100000);
			processStderr = new PipedInputStream(agentStderr, 100000);
		} catch(IOException e) {
			throw new RuntimeException("Cannot initialize Referee Agent");
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
        
        Thread t = new Thread() {
            public void run() {
                RefereeMain.start(agentStdin, new PrintStream(agentStdout));
            }
        };

        t.start();
    }

    @Override
    public String getOutput(int nbLine, long timeout) {
		if (processStdout == null) {
			return null;
		}
		try {
			byte[] tmp = new byte[REFEREE_MAX_BUFFER_SIZE];
			int offset = 0;
			int nbOccurences = 0;

			long t0 = System.nanoTime();

			while ((offset < REFEREE_MAX_BUFFER_SIZE) && (nbOccurences < nbLine)) {
				long current = System.nanoTime();
				if ((current - t0) > (timeout * 1000000L)) {
					break;
				}

				while ((offset < REFEREE_MAX_BUFFER_SIZE) && (processStdout.available() > 0)
						&& (nbOccurences < nbLine)) {
					current = System.nanoTime();
					if ((current - t0) > (timeout * 1000000L)) {
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
			processStdout = null;
		}
		return null;
	}
}