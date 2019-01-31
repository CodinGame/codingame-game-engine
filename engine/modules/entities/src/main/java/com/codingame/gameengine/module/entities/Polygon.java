package com.codingame.gameengine.module.entities;

import java.util.ArrayList;
import java.util.List;

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
    
    public Polygon addPoint(int x, int y) {
        points.add(new Point(x, y));
        return this;
    }
    
    @Override
    Entity.Type getType() {
        return Entity.Type.POLYGON;
    }
}
