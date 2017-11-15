package com.codingame.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

public class Main {
    
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        Injector injector = Guice.createInjector(new GameEngineModule());
        GameManager<? extends AbstractPlayer> gameManager = injector.getInstance(new Key<GameManager<AbstractPlayer>>() {});
        gameManager.start();
    }
}
