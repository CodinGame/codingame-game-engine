package com.codingame.gameengine.runner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class Command {
    interface CommandKey {
        String name();
    }

    static enum OutputCommand implements CommandKey {
        INIT, GET_GAME_INFO, SET_PLAYER_OUTPUT, SET_PLAYER_TIMEOUT
    }

    static enum InputCommand implements CommandKey {
        VIEW, INFOS, NEXT_PLAYER_INPUT, NEXT_PLAYER_INFO, SCORES, UINPUT, TOOLTIP, SUMMARY, METADATA, FAIL;
    }

    private List<String> lines;
    private CommandKey key;

    public Command(CommandKey key) {
        this.key = key;
        lines = new ArrayList<>();
    }

    public Command(CommandKey key, String... lines) {
        this.key = key;
        this.lines = Arrays.asList(lines);
    }

    public void addLine(Object data) {
        lines.add(String.valueOf(data));

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[[%s] %d]", key.name(), lines.size()));
        sb.append('\n');
        sb.append(lines.stream().map(line -> line + "\n").collect(Collectors.joining()));
        return sb.toString();

    }

    CommandKey getKey() {
        return key;
    }

    List<String> getLines() {
        return lines;
    }

}