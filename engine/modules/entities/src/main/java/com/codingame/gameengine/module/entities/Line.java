package com.codingame.gameengine.module.entities;

public class Line extends Shape<Line> {

    Line() {
        super();
    }
    
    public Line setX2(int x2) {
        set("x2", x2);
        return this;
    }
    public int getX2() {
        return get("x2");
    }

    public Line setY2(int y2) {
        set("y2", y2);
        return this;
    }
    public int getY2() {
        return get("y2");
    }

    @Override
    public Entity.Type getType() {
        return Entity.Type.LINE;
    }
    
}

