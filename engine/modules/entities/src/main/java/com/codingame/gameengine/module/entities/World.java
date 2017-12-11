package com.codingame.gameengine.module.entities;

public class World {
    private int width, height;

    public static final int DEFAULT_WIDTH = 1920;
    public static final int DEFAULT_HEIGHT = 1080;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public World(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public World() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

}