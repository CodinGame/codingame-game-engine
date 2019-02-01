package com.codingame.gameengine.module.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * A Polygon specifies an area in a the <code>world</code> defined by a sequence of points.
 * </p>
 * The coordinates of each point are in world units.
 */
public class Polygon extends Shape<Polygon> {

    private static class Point {
        int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

    }

    private List<Point> points = new ArrayList<>();

    Polygon() {
        super();
    }

    /**
     * Adds a point to the path of this <code>Polygon</code>.
     * 
     * @param x
     *            the x coordinate in world units
     * @param y
     *            the x coordinate in world units
     * @return this <code>Polygon</code>
     */
    public Polygon addPoint(int x, int y) {
        points.add(new Point(x, y));
        set("points", asString(points));
        return this;
    }

    /**
     * Clears the path of this <code>Polygon</code>.
     * 
     * @return this <code>Polygon</code>
     */
    public Polygon clearPoints() {
        points.clear();
        set("points", asString(points));
        return this;
    }

    /**
     * Sets the transition used to animate the positions of each point. Only used if the path has the same number of points.
     * @param curve
     *            the transition to animate between coordinates of each point in the path.
     * @return this <code>Polygon</code>
     */
    public Polygon setPointsInterpolationCurve(Curve curve) {
        set("points", asString(points), curve);
        return this;
    }

    private String asString(List<Point> points) {
        return points.stream().map(p -> p.x + "," + p.y).collect(Collectors.joining(","));
    }

    @Override
    Entity.Type getType() {
        return Entity.Type.POLYGON;
    }
}
