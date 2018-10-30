package com.codingame.gameengine.runner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.codingame.gameengine.runner.Command.CommandKey;
import com.codingame.gameengine.runner.Command.InputCommand;

class GameTurnInfo {
    private Map<CommandKey, Command> received;

    GameTurnInfo() {
        received = new HashMap<>();
    }

    void put(Command command) {
        received.put(command.getKey(), command);
    }

    boolean isComplete() {
        return isCompleteNormalTurn() || isCompleteEndTurn();
    }

    boolean isEndTurn() {
        return isCompleteEndTurn();
    }
    
    boolean refereeHasFailed() {
        return received.containsKey(InputCommand.FAIL);
    }

    private boolean isCompleteEndTurn() {
        return received.containsKey(InputCommand.SCORES)
                &&
                received.containsKey(InputCommand.VIEW)
                &&
                received.containsKey(InputCommand.INFOS);
    }

    private boolean isCompleteNormalTurn() {
        return received.containsKey(InputCommand.NEXT_PLAYER_INPUT)
                &&
                received.containsKey(InputCommand.VIEW)
                &&
                received.containsKey(InputCommand.NEXT_PLAYER_INFO)
                &&
                received.containsKey(InputCommand.INFOS);
    }

    public Optional<String> get(CommandKey key) {
        Command command = received.get(key);
        if (command == null) {
            return Optional.empty();
        }
        List<String> lines = command.getLines();
        if (lines.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(lines.get(0));
    }
}