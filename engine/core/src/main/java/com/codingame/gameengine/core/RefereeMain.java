package com.codingame.gameengine.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.util.Types;

/**
 * Entry point for the local <code>GameRunner</code> and CodinGame's server side game runner
 */
public class RefereeMain {

    private static boolean inProduction = false;

    /**
     * Is overriden by CodinGame's server side game runner 
     * @return whether or not this execution is happening locally or on CodinGame
     */
    public static boolean isInProduction() {
        return inProduction;
    }

    /**
     * CodinGame's game runner will launch the referee using this method.
     * 
     * @param args unused
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        inProduction = true;
        InputStream in = System.in;
        PrintStream out = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // Do nothing.
            }
        }));
        System.setIn(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new RuntimeException("Impossible to read from the referee");
            }
        });
        start(in, out);
    }

    /**
     * The local <code>GameRunner</code> will launch the referee using this method.
     * 
     * @param is <code>InputStream</code> used to capture the referee's stdin
     * @param out <code>PrintStream</code> used to capture the referee's stdout
     */
    @SuppressWarnings("unchecked")
    public static void start(InputStream is, PrintStream out) {

        Injector injector = Guice.createInjector(new GameEngineModule());

        Type type = Types.newParameterizedType(GameManager.class, AbstractPlayer.class);
        GameManager<AbstractPlayer> gameManager = (GameManager<AbstractPlayer>) injector.getInstance(Key.get(type));

        gameManager.start(is, out);
    }
}