package com.codingame.gameengine.module.entities;

/**
 * A RoundedRectangle specifies an area in a the <code>world</code> enclosed by the Rectangle's upper-left point (x,y), its width, and its height.
 * <p>
 * The rectangle on screen will have rounded corners
 * </p>
 * The coordinates, width and height are in world units.
 */
public class RoundedRectangle extends Shape<RoundedRectangle> {

    private int width = 100;
    private int height = 100;
    private int radius = 20;

    RoundedRectangle() {
        super();
    }

    /**
     * Sets the width of this <code>RoundedRectangle</code> in world units.
     * 
     * @param width
     *            the width for this <code>RoundedRectangle</code>.
     * @return this <code>RoundedRectangle</code>
     */
    public RoundedRectangle setWidth(int width) {
        return setWidth(width, null);
    }

    /**
     * Sets the width of this <code>RoundedRectangle</code> in world units.
     * 
     * @param width
     *            the width for this <code>RoundedRectangle</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>RoundedRectangle</code>
     */
    public RoundedRectangle setWidth(int width, Curve curve) {
        this.width = width;
        set("width", width, curve);
        return this;
    }

    /**
     * Returns the width of this <code>RoundedRectangle</code> in world units.
     * <p>
     * Default is 100.
     * 
     * @return the width of this <code>RoundedRectangle</code>.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the height of this <code>RoundedRectangle</code> in world units.
     * 
     * @param height
     *            the height for this <code>RoundedRectangle</code>.
     * @return this <code>RoundedRectangle</code>
     */
    public RoundedRectangle setHeight(int height) {
        return setHeight(height, null);
    }

    /**
     * Sets the height of this <code>RoundedRectangle</code> in world units.
     * 
     * @param height
     *            the height for this <code>RoundedRectangle</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>RoundedRectangle</code>
     */
    public RoundedRectangle setHeight(int height, Curve curve) {
        this.height = height;
        set("height", height, curve);
        return this;
    }

    /**
     * Returns the height of this <code>RoundedRectangle</code> in world units.
     * <p>
     * Default is 100.
     * 
     * @return the height of this <code>RoundedRectangle</code>.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the radius of this <code>RoundedRectangle</code>'s corners in world units.
     * 
     * @param radius
     *            the radius for the corners of this <code>RoundedRectangle</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>RoundedRectangle</code>
     */
    public RoundedRectangle setRadius(int radius, Curve curve) {
        this.radius = radius;
        set("radius", radius, curve);
        return this;
    }
    
    /**
     * Sets the radius of this <code>RoundedRectangle</code>'s corners in world units.
     * 
     * @param radius
     *            the radius for the corners of this <code>RoundedRectangle</code>.
     * @return this <code>RoundedRectangle</code>
     */
    public RoundedRectangle setRadius(int radius) {
        return setRadius(radius,null);
    }

    /**
     * Returns the radius of this <code>RoundedRectangle</code>'s corners in world units.
     * <p>
     * Default is 20.
     * </p>
     * 
     * @return the radius of the corners of this <code>RoundedRectangle</code>.
     */
    public int getRadius() {
        return radius;
    }

    @Override
    Entity.Type getType() {
        return Entity.Type.ROUNDED_RECTANGLE;
    }

}
