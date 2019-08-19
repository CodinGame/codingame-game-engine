package com.codingame.gameengine.module.entities;

/**
 * A TilingSprite is a graphical entity which displays a tiling image. That image must be loaded into the viewer's texture cache, which you can
 * configure by adding files to the <code>assets</code> folder of your game's project.
 */
public class TilingSprite extends SpriteBasedEntity<TilingSprite> {

    private int tileX, tileY;
    private double tileScaleX = 1, tileScaleY = 1;

    @Override
    Entity.Type getType() {
        return Entity.Type.TILING_SPRITE;
    }

    public TilingSprite setTileX(int tileX) {
        return setTileX(tileX, null);
    }

    public TilingSprite setTileX(int tileX, Curve curve) {
        this.tileX = tileX;
        set("tileX", tileX, curve);
        return this;
    }

    public TilingSprite setTileY(int tileY) {
        return setTileY(tileY, null);
    }

    public TilingSprite setTileY(int tileY, Curve curve) {
        this.tileY = tileY;
        set("tileY", tileY, curve);
        return this;
    }

    public TilingSprite setTileScale(double tileScale) {
        return setTileScale(tileScale, null);
    }

    public TilingSprite setTileScale(double tileScale, Curve curve) {
        setTileScaleX(tileScale, curve);
        setTileScaleY(tileScale, curve);
        return this;
    }

    public TilingSprite setTileScaleX(double tileScaleX) {
        return setTileScaleX(tileScaleX, null);
    }

    public TilingSprite setTileScaleX(double tileScaleX, Curve curve) {
        this.tileScaleX = tileScaleX;
        set("tileScaleX", tileScaleX, curve);
        return this;
    }

    public TilingSprite setTileScaleY(double tileScaleY) {
        return setTileScaleY(tileScaleY, null);
    }

    public TilingSprite setTileScaleY(double tileScaleY, Curve curve) {
        this.tileScaleY = tileScaleY;
        set("tileScaleY", tileScaleY, curve);
        return this;
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public double getTileScaleX() {
        return tileScaleX;
    }

    public double getTileScaleY() {
        return tileScaleY;
    }

}