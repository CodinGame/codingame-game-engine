package com.codingame.gameengine.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;

public class ActionManager {
    private List<Action> actions = new ArrayList<>();
    
    public void setActions(Action... actions) {
        setActions(Arrays.asList(actions));
    }
    
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public List<Action> handlePlayerOutputs(AbstractPlayer player) throws TimeoutException {
        if (actions == null || actions.isEmpty()) {
            throw new RuntimeException("Actions are not set.");
        }
        
        List<String> playerOutput = player.getOutputs();

        return playerOutput.stream()
            .map(
                command -> actions.stream().filter(a -> a.matches(command)).findFirst()
                )
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }
}
