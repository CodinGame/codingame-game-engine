package com.codingame.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.Inject;

@Singleton
public final class GameManager<T extends AbstractPlayer> {
    private List<T> players;
    private int turnMaxTime = 50;
    private int maxTurn = 200;
    
    @Inject Provider<T> playerProvider;
    @Inject Provider<Referee> refereeProvider;
    
    public List<T> getPlayers() {
        return players;
    }

    public T getPlayer(int index) {
        return players.get(index);
    }

    public List<T> getActivePlayers() {
        return players.stream().filter(AbstractPlayer::isActive).collect(Collectors.toList());
    }

    public void sendInputsToPlayers() {
        for (T player : getActivePlayers()) {
            player.getInputs();
            // call player
            player.setOutputs(new ArrayList<String>());
        }
    }

    public void endGame() {
    }

    public void setTurnMaxTime(int turnMaxTime) {
        this.turnMaxTime = turnMaxTime;
    }

    public void setMaxTurn(int maxTurn) {
        this.maxTurn = maxTurn;
    }

    int getMaxTurn() {
        return maxTurn;
    }

    int getTurnMaxTime() {
        return turnMaxTime;
    }

    public void start() {
        System.out.println(playerProvider.get().getClass().getName());
        System.out.println(refereeProvider.get().getClass().getName());
    }

}
