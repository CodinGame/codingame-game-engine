package com.codingame.gameengine.runner.dto;

import java.util.Map;

public class GameScore {

    public Map<Integer, Integer> scores;

    public GameScore(Map<Integer, Integer> scores) {
        this.scores = scores;
    }
}
