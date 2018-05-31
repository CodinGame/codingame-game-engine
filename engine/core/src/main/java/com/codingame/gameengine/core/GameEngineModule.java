package com.codingame.gameengine.core;

import java.lang.reflect.Type;

import javax.inject.Provider;

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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Provides
    @Singleton
    GameManager<AbstractPlayer> provideGameManager(
        Injector injector, Provider<SoloGameManager<AbstractSoloPlayer>> soloProvider,
        Provider<MultiplayerGameManager<AbstractMultiplayerPlayer>> multiProvider
    ) throws ClassNotFoundException {
        if (isMulti()) {
            return (GameManager) multiProvider.get();
        } else if (isSolo()) {
            return (GameManager) soloProvider.get();
        } else {
            throw new RuntimeException("Unknown game mode");
        }
    }

    @SuppressWarnings("unchecked")
    @Provides
    @Singleton
    MultiplayerGameManager<AbstractMultiplayerPlayer> provideMultiplayerGameManager(Injector injector) throws ClassNotFoundException {
        if (isMulti()) {
            Type type = Types.newParameterizedType(MultiplayerGameManager.class, getPlayerClass());
            MultiplayerGameManager<AbstractMultiplayerPlayer> gameManager = (MultiplayerGameManager<AbstractMultiplayerPlayer>) injector
                .getInstance(Key.get(type));
            return gameManager;
        } else if (isSolo()) {
            throw new RuntimeException("Cannot use MultiplayerGameManager in a solo player game");
        } else {
            throw new RuntimeException("Unknown game mode");
        }
    }

    @SuppressWarnings("unchecked")
    @Provides
    @Singleton
    SoloGameManager<AbstractSoloPlayer> provideSoloGameManager(Injector injector) throws ClassNotFoundException {
        if (isSolo()) {
            Type type = Types.newParameterizedType(SoloGameManager.class, getPlayerClass());
            SoloGameManager<AbstractSoloPlayer> gameManager = (SoloGameManager<AbstractSoloPlayer>) injector.getInstance(Key.get(type));
            return gameManager;
        } else if (isMulti()) {
            throw new RuntimeException("Cannot use SoloGameManager in a multiplayer game");
        } else {
            throw new RuntimeException("Unknown game mode");
        }
    }

    private boolean isMulti() {
        return "multi".equals(System.getProperty("game.mode"));
    }

    private boolean isSolo() {
        return "solo".equals(System.getProperty("game.mode"));
    }
}