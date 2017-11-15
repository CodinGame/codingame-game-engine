package com.codingame.core;

import java.util.Set;

import org.reflections.Reflections;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {
    
    private static Injector injector = Guice.createInjector();
    
    private static AbstractReferee getNewRefereeInstance() throws InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections();
        Set<Class<? extends AbstractReferee>> abstractReferees = reflections.getSubTypesOf(AbstractReferee.class);
        if (abstractReferees.size() == 0) throw new RuntimeException("Referee class not found");
        if (abstractReferees.size() > 1) throw new RuntimeException(String.format("More than 1 referee class found: %s", abstractReferees.toString()));

        Class<? extends AbstractReferee> refereeClass = abstractReferees.iterator().next();

        AbstractReferee abstractReferee = refereeClass.newInstance();
        injector.injectMembers(abstractReferee);
        
        return abstractReferee;
    }

    private static AbstractPlayer getNewPlayerInstance() throws InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections();
        Set<Class<? extends AbstractPlayer>> abstractPlayers = reflections.getSubTypesOf(AbstractPlayer.class);
        if (abstractPlayers.size() == 0) throw new RuntimeException("Player class not found");
        if (abstractPlayers.size() > 1) throw new RuntimeException(String.format("More than 1 player class found: %s", abstractPlayers.toString()));

        Class<? extends AbstractPlayer> playerClass = abstractPlayers.iterator().next();

        AbstractPlayer abstractPlayer = playerClass.newInstance();
        injector.injectMembers(abstractPlayer);
        
        return abstractPlayer;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {

        // just some tests
        
        AbstractReferee referee = getNewRefereeInstance();
        AbstractPlayer player = getNewPlayerInstance();
        
        player.setActive(true);

        referee.init(null);
        referee.gameTurn(1);
        
        GameManager<? extends AbstractPlayer> gameManager = injector.getInstance(GameManager.class);
        System.out.println(gameManager.getMaxTurn());
        System.out.println(gameManager.getTurnMaxTime());

        referee.gameTurn(1);
        referee.onEnd();

    }

}
