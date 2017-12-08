package com.codingame.gameengine.core;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.util.Types;

public class Main {

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        start(System.in, System.out);
    }

    @SuppressWarnings("unchecked")
    public static void start(InputStream is, PrintStream out) {
        Injector injector = Guice.createInjector(new GameEngineModule());

        Type type = Types.newParameterizedType(GameManager.class, AbstractPlayer.class);
        GameManager<AbstractPlayer> gameManager = (GameManager<AbstractPlayer>) injector.getInstance(Key.get(type));       
        
        gameManager.start(is, out);
    }
}