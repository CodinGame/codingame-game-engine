package com.codingame.gameengine.module.entities;

/**
 * The World represents a coordinate mapping from the positions of entities to the pixels on screen. The viewer's canvas has 1920x1080 pixels and so
 * the default World's width and height are also 1920 and 1080.
 *
 */
public class World {
    private int width, height;

    /**
     * The default World's width.
     */
    public static final int DEFAULT_WIDTH = 1920;

    /**
     * The default World's height.
     */
    public static final int DEFAULT_HEIGHT = 1080;

    /**
     * Returns the width of this world.
     * 
     * @return the width of this world.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of this world.
     * 
     * @return the height of this world.
     */
    public int getHeight() {
        return height;
    }

    World(int width, int height) {
        this.width = width;
        this.height = height;
    }

    World() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

}