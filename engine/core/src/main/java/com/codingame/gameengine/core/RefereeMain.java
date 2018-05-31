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

public class RefereeMain {

    private static boolean inProduction = false;

    public static boolean isInProduction() {
        return inProduction;
    }

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

    @SuppressWarnings("unchecked")
    public static void start(InputStream is, PrintStream out) {

        Injector injector = Guice.createInjector(new GameEngineModule());

        Type type = Types.newParameterizedType(GameManager.class, AbstractPlayer.class);
        GameManager<AbstractPlayer> gameManager = (GameManager<AbstractPlayer>) injector.getInstance(Key.get(type));

        gameManager.start(is, out);
    }
}