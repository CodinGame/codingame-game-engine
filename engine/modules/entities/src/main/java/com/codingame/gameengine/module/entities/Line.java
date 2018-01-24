package com.codingame.gameengine.module.entities;

/**
 * Represents a line segment from (x,y) to (x2,y2) in world units. This Shape does not have a fill.
 */
public class Line extends Shape<Line> {

    private int x2, y2;

    Line() {
        super();
    }

    /**
     * Sets the X coordinate of the end point of this <code>Line</code>.
     * <p>
     * The starting point of this line is its position.
     * 
     * @param x2
     *            the X coordinate for the end point of this <code>Line</code>.
     * @return this <code>Line</code>.
     */
    public Line setX2(int x2) {
        return setX2(x2, null);
    }

    /**
     * Sets the X coordinate of the end point of this <code>Line</code>.
     * <p>
     * The starting point of this line is its position.
     * 
     * @param x2
     *            the X coordinate for the end point of this <code>Line</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Line</code>.
     */
    public Line setX2(int x2, Curve curve) {
        this.x2 = x2;
        set("x2", x2, curve);
        return this;
    }

    /**
     * Returns the X coordinate of this <code>Line</code>'s end point.
     * 
     * @return the X coordinate of this <code>Line</code>'s end point.
     */
    public int getX2() {
        return x2;
    }

    /**
     * Sets the y coordinate of the end point of this <code>Line</code>.
     * <p>
     * The starting point of this line is its position.
     * 
     * @param y2
     *            the Y coordinate for the end point of this <code>Line</code>.
     * @return this <code>Line</code>.
     */
    public Line setY2(int y2) {
        return setY2(y2, null);
    }

    /**
     * Sets the y coordinate of the end point of this <code>Line</code>.
     * <p>
     * The starting point of this line is its position.
     * 
     * @param y2
     *            the Y coordinate for the end point of this <code>Line</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Line</code>.
     */
    public Line setY2(int y2, Curve curve) {
        this.y2 = y2;
        set("y2", y2, curve);
        return this;
    }

    /**
     * Returns the Y coordinate of this <code>Line</code>'s end point.
     * 
     * @return the Y coordinate of this <code>Line</code>'s end point.
     */
    public int getY2() {
        return y2;
    }

    @Override
    Entity.Type getType() {
        return Entity.Type.LINE;
    }

}
