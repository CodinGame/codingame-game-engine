package com.codingame.gameengine.core;

import java.lang.reflect.Type;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.util.Types;

class GameEngineModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @SuppressWarnings("unchecked")
    private Class<? extends AbstractPlayer> getPlayerClass() throws ClassNotFoundException {
        return (Class<? extends AbstractPlayer>) Class.forName("com.codingame.game.Player");
    }

    @SuppressWarnings("unchecked")
    private Class<? extends AbstractReferee> getRefereeClass() throws ClassNotFoundException {
        return (Class<? extends AbstractReferee>) Class.forName("com.codingame.game.Referee");
    }

    @Provides
    @Singleton
    AbstractReferee provideAbstractReferee(Injector injector) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        AbstractReferee referee = getRefereeClass().newInstance();
        injector.injectMembers(referee);
        return referee;
    }

    @Provides
    AbstractPlayer providePlayer(Injector injector) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        AbstractPlayer abstractPlayer = getPlayerClass().newInstance();
        injector.injectMembers(abstractPlayer);

        return abstractPlayer;
    }

    @SuppressWarnings("unchecked")
    @Provides
    @Singleton
    GameManager<AbstractPlayer> provideGameManager(Injector injector) throws ClassNotFoundException {
        Type type = Types.newParameterizedType(GameManager.class, getPlayerClass());
        GameManager<AbstractPlayer> gameManager = (GameManager<AbstractPlayer>) injector.getInstance(Key.get(type));

        return gameManager;
    }
}