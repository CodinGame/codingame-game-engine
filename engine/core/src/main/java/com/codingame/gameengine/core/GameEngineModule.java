package com.codingame.gameengine.core;

import java.lang.reflect.Type;
import java.util.Set;

import org.reflections.Reflections;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.util.Types;

public class GameEngineModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    private Class<? extends AbstractPlayer> getPlayerClass() {
        Reflections reflections = RefereeMain.isInProduction() ? Reflections.collect() : new Reflections("");
        Set<Class<? extends AbstractPlayer>> abstractPlayers = reflections.getSubTypesOf(AbstractPlayer.class);
        if (abstractPlayers.size() == 0) throw new RuntimeException("Player class not found");
        if (abstractPlayers.size() > 1) throw new RuntimeException(String.format("More than 1 player class found: %s", abstractPlayers.toString()));

        Class<? extends AbstractPlayer> playerClass = abstractPlayers.iterator().next();

        return playerClass;
    }

    @Provides
    @Singleton
    Referee provideReferee(Injector injector) throws InstantiationException, IllegalAccessException {
        Reflections reflections = RefereeMain.isInProduction() ? Reflections.collect() : new Reflections("");
        Set<Class<? extends Referee>> referees = reflections.getSubTypesOf(Referee.class);
        if (referees.size() == 0) throw new RuntimeException("Referee class not found");
        if (referees.size() > 1) throw new RuntimeException(String.format("More than 1 referee class found: %s", referees.toString()));

        Class<? extends Referee> refereeClass = referees.iterator().next();

        Referee referee = refereeClass.newInstance();
        injector.injectMembers(referee);
        return referee;
    }

    @Provides
    AbstractPlayer providePlayer(Injector injector) throws InstantiationException, IllegalAccessException {
        AbstractPlayer abstractPlayer = getPlayerClass().newInstance();
        injector.injectMembers(abstractPlayer);

        return abstractPlayer;
    }

    @SuppressWarnings("unchecked")
    @Provides
    @Singleton
    GameManager<AbstractPlayer> provideGameManager(Injector injector) {
        Type type = Types.newParameterizedType(GameManager.class, getPlayerClass());
        GameManager<AbstractPlayer> gameManager = (GameManager<AbstractPlayer>) injector.getInstance(Key.get(type));

        return gameManager;
    }
}