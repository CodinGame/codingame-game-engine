package com.codingame.gameengine.module.entities;

abstract public class Entity<T extends Entity<?>> {
    final int id;
    EntityState state;
    EntityState oldMap; 
    //TODO: oldMap is useless? (nope)
    
    static enum Type {
        CIRCLE, LINE, RECTANGLE, SPRITE
    }    

    Entity() {
        id = ++EntityManager.ENTITY_COUNT;
        state = new EntityState();
        oldMap = state;
        
        set("x", 0);
        set("y", 0);
    }
    
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    public int getId() {
        return id;
    }
    
    @SuppressWarnings("unchecked")
    protected <P> P get(String key) {
        return (P) state.get(key);
    }

    public int getY() {
        return get("y");
    }
    public T setY(int y) {
        set("y", y);
        return self();
    }
    
    protected void set(String key, Object value) {
        state.put(key, value);    
    }
    
    public int getX() {
        return get("x");
    }

    public T setX(int x) {
        set("x", x);
        return self();
    }

    abstract public Type getType();

    public T setZIndex(int zIndex) {
        set("zIndex", zIndex);
        return self();
    }
    public int getZIndex() {
        return get("zIndex");
    }

    public T setScale(double scale) {
        set("scaleX", scale);
        set("scaleY", scale);
        return self();
    }
    
    public T setScaleX(double scaleX) {
        set("scaleX", scaleX);
        return self();
    }
    
    public int getScaleX() {
        return get("scaleX");
    }

    public T setScaleY(double scaleY) {
        set("scaleY", scaleY);
        return self();
    }
    public int getScaleY() {
        return get("scaleY");
    }

    
}