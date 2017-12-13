package com.codingame.gameengine.core;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class InputCommand {
    public static enum Command {
        INIT, GET_GAME_INFO, SET_PLAYER_OUTPUT, SET_PLAYER_TIMEOUT
    }

    public Command cmd;
    public int lineCount;

    public InputCommand(Command cmd, int lineCount) {
        this.cmd = cmd;
        this.lineCount = lineCount;
    }

    static InputCommand parse(String line) {
        final Pattern HEADER_PATTERN = Pattern.compile("\\[\\[(?<cmd>.+)\\] ?(?<lineCount>[0-9]+)\\]");
        
        Matcher m = HEADER_PATTERN.matcher(line);
        if (!m.matches())
            throw new RuntimeException("Error in data sent to referee");
        
        Command cmd = Command.valueOf(m.group("cmd"));
        int lineCount = Integer.parseInt(m.group("lineCount"));
        
        return new InputCommand(cmd, lineCount);
    }
}
