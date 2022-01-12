package com.codingame.gameengine.core;

public final class TimeoutSettings {

    private static final int MIN_TURN_TIME  =     50;
    private static final int MAX_TURN_TIME  = 25_000;
    private static final int MAX_TOTAL_TIME = 30_000; // TODO: "hard quota"?

    private Mode mode;
    private int limit; // ms
    private int limitFirst;

    public TimeoutSettings() {
        set(50, 1000);
    }

    public void set(final int total) {
        if (total < MIN_TURN_TIME || total > MAX_TOTAL_TIME)
            throw new IllegalArgumentException("`total` cannot be less than 50ms or greater than 30s");

        mode = Mode.TOTAL;
        limit = total;
        limitFirst = -1;
    }

    public void set(final int perTurn, final int first) {
        if (perTurn < MIN_TURN_TIME || perTurn > MAX_TURN_TIME)
            throw new IllegalArgumentException("`perTurn` cannot be less than 50ms or greater than 25s");
        if (first < MIN_TURN_TIME || first > MAX_TURN_TIME)
            throw new IllegalArgumentException("`first` cannot be less than 50ms or greater than 25s");

        mode = Mode.PER_TURN;
        limit = perTurn;
        limitFirst = first;
    }

    public <T extends AbstractPlayer> int getMaxTurnTime(final T player) {
        if (mode == Mode.PER_TURN)
            return player.hasNeverBeenExecuted() ? limitFirst : limit;
        else
            return Math.max(limit - player.getTotalTimeSpentMs(), 0);
    }

    private enum Mode { PER_TURN, TOTAL }

}
