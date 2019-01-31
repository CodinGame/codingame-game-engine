package com.codingame.gameengine.module.entities;

/**
 * <p>
 * A Circle specifies an area in a the <code>world</code> defined by the Circle's center point (x,y) and its radius. 
 * </p>
 * The coordinates and radius are in world units.
 */
public class Circle extends Shape<Circle> {

    private int radius = 100;

    Circle() {
        super();
    }

    /**
     * Sets the radius of this <code>Circle</code> in world units.
     * 
     * 
     * @param radius
     *            the radius for this <code>Circle</code>.
     * @throws IllegalArgumentException
     *             if radius &lt; 0
     * @return this <code>Circle</code>.
     */
    public Circle setRadius(int radius) {
        return setRadius(radius, null);
    }

    /**
     * Sets the radius of this <code>Circle</code> in world units.
     * 
     * 
     * @param radius
     *            the radius for this <code>Circle</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @throws IllegalArgumentException
     *             if radius &lt; 0
     * 
     * @return this <code>Circle</code>.
     */
    public Circle setRadius(int radius, Curve curve) {
        if (radius < 0) {
            throw new IllegalArgumentException("A Circle's radius may not be less than zero");
        }
        this.radius = radius;
        set("radius", radius, curve);
        return this;
    }

    /**
     * <p>
     * Returns the radius of this <code>Circle</code> in world units. 
     * </p>
     * Default is 100.
     * 
     * @return the radius of this <code>Circle</code>.
     */
    public int getRadius() {
        return radius;
    }

    @Override
    Entity.Type getType() {
        return Entity.Type.CIRCLE;
    }
}
