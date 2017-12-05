package com.codingame.gameengine.module.entities;

public class Rectangle extends Shape<Rectangle> {

    Rectangle() {
        super();
    }
    
    public Rectangle setWidth(int width) {
        set("width", width);
        return this;
    }
    public int getWidth() {
        return get("width");
    }

    public Rectangle setHeight(int height) {
        set("height", height);
        return this;
    }
    public int getHeight() {
        return get("height");
    }
    @Override
    public Entity.Type getType() {
        return Entity.Type.RECTANGLE;
    }
    
}

