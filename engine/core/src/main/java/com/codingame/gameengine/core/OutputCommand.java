package com.codingame.gameengine.core;
enum OutputCommand {
    VIEW, INFOS, NEXT_PLAYER_INPUT, NEXT_PLAYER_INFO, SCORES, UINPUT, TOOLTIP, SUMMARY, METADATA, FAIL;
    public String format(int lineCount) {
        return String.format("[[%s] %d]", this.name(), lineCount);
    }
}
