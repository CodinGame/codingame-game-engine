package com.codingame.gameengine.module.entities;

/**
 * A Sprite is a graphical entity which displays an image. That image must be loaded into the viewer's texture cache, which you can configure by
 * adding files to the <code>assets</code> folder of your game's project.
 */
public class Sprite extends SpriteBasedEntity<Sprite> {

    @Override
    Entity.Type getType() {
        return Entity.Type.SPRITE;
    }

}