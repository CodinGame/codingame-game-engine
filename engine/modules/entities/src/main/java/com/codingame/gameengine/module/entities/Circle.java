package com.codingame.gameengine.module.entities;

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
        if (radius < 0) {
            throw new IllegalArgumentException("A Circle's radius may not be less than zero");
        }
        this.radius = radius;
        set("radius", radius);
        return this;
    }

    /**
     * Returns the radius of this <code>Circle</code> in world units.
     * <p>
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
