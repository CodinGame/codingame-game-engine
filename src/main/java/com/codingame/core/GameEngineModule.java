package com.codingame.core;

import java.util.Set;

import org.reflections.Reflections;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class GameEngineModule extends AbstractModule {

    @Override
    protected void configure() {
    }
    
    @Provides
    @Singleton
    Referee provideReferee(Injector injector) throws InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections("");
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
        Reflections reflections = new Reflections("");
        Set<Class<? extends AbstractPlayer>> abstractPlayers = reflections.getSubTypesOf(AbstractPlayer.class);
        if (abstractPlayers.size() == 0) throw new RuntimeException("Player class not found");
        if (abstractPlayers.size() > 1) throw new RuntimeException(String.format("More than 1 player class found: %s", abstractPlayers.toString()));

        Class<? extends AbstractPlayer> playerClass = abstractPlayers.iterator().next();

        AbstractPlayer abstractPlayer = playerClass.newInstance();
        injector.injectMembers(abstractPlayer);
        
        return abstractPlayer;
    }
}
