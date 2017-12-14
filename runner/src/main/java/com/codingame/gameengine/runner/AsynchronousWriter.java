package com.codingame.gameengine.runner;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

class AsynchronousWriter extends Thread {

    private final BlockingQueue<String> queue;
    private final OutputStream stream;
    private boolean interrupt;

    public AsynchronousWriter(BlockingQueue<String> queue, OutputStream stream) {
        this.queue = queue;
        this.stream = stream;
        this.interrupt = false;
    }

    @Override
    public void run() {
        while (!interrupt) {
            try {
                String toWrite = queue.take();
                if (this.stream == null || toWrite == null || GameRunner.INTERRUPT_THREAD.equals(toWrite)) {
                    interrupt = true;
                } else {
                    stream.write(toWrite.getBytes(Agent.UTF8));
                    stream.flush();
                }
            } catch (InterruptedException | IOException e) {
                interrupt = true;
            }
        }
    }

}