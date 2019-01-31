package com.codingame.gameengine.module.entities;

/**
 * <p>
 * A Rectangle specifies an area in a the <code>world</code> enclosed by the Rectangle's upper-left point (x,y), its width, and its height.
 * </p>
 * The coordinates, width and height are in world units.
 */
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
        return setWidth(width, null);
    }

    /**
     * Sets the width of this <code>Rectangle</code> in world units.
     * 
     * @param width
     *            the width for this <code>Rectangle</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Rectangle</code>
     */
    public Rectangle setWidth(int width, Curve curve) {
        this.width = width;
        set("width", width, curve);
        return this;
    }

    /**
     * Returns the width of this <code>Rectangle</code> in world units.
     * <p>
     * Default is 100.
     * </p>
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
        return setHeight(height, null);
    }

    /**
     * Sets the height of this <code>Rectangle</code> in world units.
     * 
     * @param height
     *            the height for this <code>Rectangle</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Rectangle</code>
     */
    public Rectangle setHeight(int height, Curve curve) {
        this.height = height;
        set("height", height, curve);
        return this;
    }

    /**
     * Returns the height of this <code>Rectangle</code> in world units.
     * <p>
     * Default is 100.
     * </p>
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
