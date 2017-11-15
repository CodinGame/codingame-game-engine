package com.codingame.core;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

@Singleton
public final class GameManager<T extends AbstractPlayer> {
    private List<T> players;
    private int turnMaxTime = 50;
    private int maxTurn = 200;

    public List<T> getPlayers() {
        return players;
    }

    public T getPlayer(int index) {
        return players.get(index);
    }

    public List<T> getActivePlayers() {
        List<T> actives = new ArrayList<T>();
        for (T player : players) {
            if (player.isActive()) {
                actives.add(player);
            }
        }

        return actives;
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

}
