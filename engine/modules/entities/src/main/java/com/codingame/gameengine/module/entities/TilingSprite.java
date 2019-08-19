package com.codingame.gameengine.module.entities;

/**
 * A TilingSprite is a graphical entity which displays a tiling image. That image must be loaded into the viewer's texture cache, which you can
 * configure by adding files to the <code>assets</code> folder of your game's project.
 * 
 * @see <a href="http://pixijs.download/v4.8.5/docs/PIXI.extras.TilingSprite.html">PIXI TilingSprite</a>
 */
public class TilingSprite extends SpriteBasedEntity<TilingSprite> {

    private int tileX, tileY;
    private double tileScaleX = 1, tileScaleY = 1;

    @Override
    Entity.Type getType() {
        return Entity.Type.TILING_SPRITE;
    }

    /**
     * Sets the X offset of the image that is being tiled.
     * 
     * @param tileX
     *            the X offset of the image that is being tiled
     * @return this <code>Entity</code>
     */
    public TilingSprite setTileX(int tileX) {
        return setTileX(tileX, null);
    }

    /**
     * Sets the X offset of the image that is being tiled.
     * 
     * @param tileX
     *            the X offset of the image that is being tiled
     * @param curve
     *            the transition to animate between values of this property
     * @return this <code>Entity</code>
     */
    public TilingSprite setTileX(int tileX, Curve curve) {
        this.tileX = tileX;
        set("tileX", tileX, curve);
        return this;
    }

    /**
     * Sets the Y offset of the image that is being tiled.
     * 
     * @param tileY
     *            the Y offset of the image that is being tiled
     * @return this <code>Entity</code>
     */
    public TilingSprite setTileY(int tileY) {
        return setTileY(tileY, null);
    }

    /**
     * Sets the Y offset of the image that is being tiled.
     * 
     * @param tileY
     *            the Y offset of the image that is being tiled
     * @param curve
     *            the transition to animate between values of this property
     * @return this <code>Entity</code>
     */
    public TilingSprite setTileY(int tileY, Curve curve) {
        this.tileY = tileY;
        set("tileY", tileY, curve);
        return this;
    }

    /**
     * Sets both the horizontal and vertical scale of the image that is being tiled.
     * 
     * @param tileScale
     *            the scale of the image that is being tiled
     * @return this <code>Entity</code>
     */
    public TilingSprite setTileScale(double tileScale) {
        return setTileScale(tileScale, null);
    }

    /**
     * Sets both the horizontal and vertical scale of the image that is being tiled.
     * 
     * @param tileScale
     *            the scale of the image that is being tiled
     * @param curve
     *            the transition to animate between values of this property
     * @return this <code>Entity</code>
     */
    public TilingSprite setTileScale(double tileScale, Curve curve) {
        setTileScaleX(tileScale, curve);
        setTileScaleY(tileScale, curve);
        return this;
    }

    /**
     * Sets the horizontal scale of the image that is being tiled.
     * 
     * @param tileScaleX
     *            the horizontal scale of the image that is being tiled
     * @return this <code>Entity</code>
     */
    public TilingSprite setTileScaleX(double tileScaleX) {
        return setTileScaleX(tileScaleX, null);
    }

    /**
     * Sets the horizontal scale of the image that is being tiled.
     * 
     * @param tileScaleX
     *            the horizontal scale of the image that is being tiled
     * @param curve
     *            the transition to animate between values of this property
     * @return this <code>Entity</code>
     */
    public TilingSprite setTileScaleX(double tileScaleX, Curve curve) {
        this.tileScaleX = tileScaleX;
        set("tileScaleX", tileScaleX, curve);
        return this;
    }

    /**
     * Sets the vertical scale of the image that is being tiled.
     * 
     * @param tileScaleY
     *            the vertical scale of the image that is being tiled
     * @return this <code>Entity</code>
     */
    public TilingSprite setTileScaleY(double tileScaleY) {
        return setTileScaleY(tileScaleY, null);
    }

    /**
     * Sets the vertical scale of the image that is being tiled.
     * 
     * @param tileScaleY
     *            the vertical scale of the image that is being tiled
     * @param curve
     *            the transition to animate between values of this property
     * @return this <code>Entity</code>
     */
    public TilingSprite setTileScaleY(double tileScaleY, Curve curve) {
        this.tileScaleY = tileScaleY;
        set("tileScaleY", tileScaleY, curve);
        return this;
    }

    /**
     * Sets the X offset of the image that is being tiled.
     * 
     * @return the X offset of the image that is being tiled
     */
    public int getTileX() {
        return tileX;
    }

    /**
     * Sets the Y offset of the image that is being tiled.
     * 
     * @return the Y offset of the image that is being tiled
     */
    public int getTileY() {
        return tileY;
    }

    /**
     * Gets the horizontal scale of the image that is being tiled.
     * 
     * @return the horizontal scale of the image that is being tiled
     */
    public double getTileScaleX() {
        return tileScaleX;
    }

    /**
     * Gets the vertical scale of the image that is being tiled.
     * 
     * @return the vertical scale of the image that is being tiled
     */
    public double getTileScaleY() {
        return tileScaleY;
    }

}