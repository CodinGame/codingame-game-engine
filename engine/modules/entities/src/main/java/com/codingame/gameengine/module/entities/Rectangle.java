package com.codingame.gameengine.module.entities;

public class Rectangle extends Shape<Rectangle> {

    private int width, height;

    Rectangle() {
        super();
    }

    /**
     * Sets the width of this <code>Rectangle</code> in world units.
     * 
     * @param width
     *            the width for this <code>Rectangle</code>.
     * @return this <code>Rectangle</code>
     */
    public Rectangle setWidth(int width) {
        this.width = width;
        set("width", width);
        return this;
    }

    /**
     * Returns the width of this <code>Rectangle</code> in world units.
     * <p>
     * Default is 100.
     * 
     * @return the width of this <code>Rectangle</code>.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the height of this <code>Rectangle</code> in world units.
     * 
     * @param height
     *            the height for this <code>Rectangle</code>.
     * @return this <code>Rectangle</code>
     */
    public Rectangle setHeight(int height) {
        this.height = height;
        set("height", height);
        return this;
    }

    /**
     * Returns the height of this <code>Rectangle</code> in world units.
     * <p>
     * Default is 100.
     * 
     * @return the height of this <code>Rectangle</code>.
     */
    public int getHeight() {
        return height;
    }

    @Override
    Entity.Type getType() {
        return Entity.Type.RECTANGLE;
    }

}
