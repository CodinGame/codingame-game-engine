package com.codingame.gameengine.module.entities;

abstract public class Shape<T extends Entity<?>> extends Entity<T> {

    Shape() {
        super();
    }

    public T setFillColor(int color) {
        set("fillColor", color);
        return self();
    }
    public int getFillColor() {
        return get("fillColor");
    }

    public T setFillAlpha(double alpha) {
        set("fillAlpha", alpha);
        return self();
    }
    public double getFillAlpha() {
        return get("fillAlpha");
    }
    
    public T setLineAlpha(double alpha) {
        set("lineAlpha", alpha);
        return self();
    }
    public int getLineAlpha() {
        return get("lineAlpha");
    }

    public T setLineWidth(int lineWidth) {
        set("lineWidth", lineWidth);
        return self();
    }
    public int getLineWidth() {
        return get("lineWidth");
    }

    public T setLineColor(int lineColor) {
        set("lineColor", lineColor);
        return self();
    }
    public int getLineColor() {
        return get("lineColor");
    }

}