package com.codingame.gameengine.module.entities;

public class Sprite extends Shape<Sprite> {

    Sprite() {
        super();
    }
    
    public Sprite setImage(String string) {
        set("image", string);
        return this;
    }
    public String getImage() {
        return get("image");
    }

    @Override
    public Entity.Type getType() {
        return Entity.Type.SPRITE;
    }
    
}

