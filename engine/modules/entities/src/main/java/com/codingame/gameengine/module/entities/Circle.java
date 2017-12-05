package com.codingame.gameengine.module.entities;

public class Circle extends Shape<Circle> {

    Circle() {
        super();
    }
    
    public Circle setRadius(int radius) {
        set("radius", radius);
        return this;
    }
    
    //TODO: default values
    public int getRadius() {
        return get("radius");
    }

    @Override
    public Entity.Type getType() {
        return Entity.Type.CIRCLE;
    }    
}

