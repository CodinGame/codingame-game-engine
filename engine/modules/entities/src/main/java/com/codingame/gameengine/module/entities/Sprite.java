package com.codingame.gameengine.module.entities;

public class Sprite extends Shape<Sprite> {

    /**
     * The list of supported PIXI blend modes and their associated constant.
     * 
     * @see <a href="http://pixijs.download/dev/docs/PIXI.html#.BLEND_MODES">PIXI BLEND_MODES</a>
     */
    public static enum BlendMode {
        NORMAL(0), ADD(1), MULTIPLY(2), SCREEN(3);
        private int value;

        private BlendMode(int value) {
            this.value = value;
        }

        private int getValue() {
            return value;
        }
    }

    private String image;
    private BlendMode blendMode;
    private double anchorX = 0.5, anchorY = 0.5;
    private int tint = 0xFFFFFF;

    Sprite() {
        super();
    }

    @Override
    Entity.Type getType() {
        return Entity.Type.SPRITE;
    }

    /**
     * Sets the image for this <code>Sprite</code>.
     * <p>
     * You must either:
     * <ul>
     * <li>use the name of an image manually referenced in the config.js file of the JavaScript project.
     * <li>use the a player's nickname token.
     * </ul>
     * 
     * @param image
     *            the name of the image to use for this <code>Sprite</code>.
     * @return this <code>Sprite</code>.
     */
    public Sprite setImage(String image) {
        this.image = image;
        set("image", image);
        return this;
    }

    /**
     * Returns the name of the image used for this <code>Sprite</code>.
     * <p>
     * Can be a player's nickname token.
     * 
     * @return the name of the image used for this <code>Sprite</code>
     */
    public String getImage() {
        return image;
    }

    /**
     * Returns the <code>BlendMode</code> this <code>Sprite</code> is to be drawn with.
     * 
     * @see <a href="http://pixijs.download/dev/docs/PIXI.html#.BLEND_MODES">PIXI BLEND_MODES</a>
     * @return the <code>BlendMode</code> this <code>Sprite</code> is to be drawn with.
     */
    public BlendMode getBlendMode() {
        return blendMode;
    }

    /**
     * Sets the blend mode for this <code>Sprite</code>.
     * <p>
     * The possible values are found in <code>BlendMode</code>.
     * 
     * @see <a href="http://pixijs.download/dev/docs/PIXI.html#.BLEND_MODES">PIXI BLEND_MODES</a>
     * @param blendMode
     * @return this <code>Sprite</code>.
     */
    public Sprite setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
        set("blendMode", blendMode.getValue());
        return this;
    }

    /**
     * Sets both the X and Y anchors of this <code>Sprite</code> as a percentage of its width and height.
     * <p>
     * The point calculated by the anchors times the size of this <code>Sprite</code> will be the origin for any rotation or scale.
     * 
     * @param anchor
     *            the percentage for both anchors of this <code>Sprite</code>.
     * @return this <code>Sprite</code>.
     */
    public Sprite setAnchor(double anchor) {
        setAnchorX(anchor);
        setAnchorY(anchor);
        return this;
    }

    /**
     * Returns the X anchor of this <code>Sprite</code> as a percentage of its width.
     * <p>
     * Default is 0.5.
     * 
     * @return the X anchor of this <code>Sprite</code> as a percentage of its width.
     */
    public double getAnchorX() {
        return anchorX;
    }

    /**
     * Sets the X anchor of this <code>Sprite</code> as a percentage of its width.
     * <p>
     * The point calculated by the anchors times the size of this <code>Sprite</code> will be the origin for any rotation or scale.
     * 
     * @param anchorX
     *            the X anchor for this <code>Sprite</code>.
     * @return this <code>Sprite</code>.
     */
    public Sprite setAnchorX(double anchorX) {
        this.anchorX = anchorX;
        set("anchorX", anchorX);
        return this;
    }

    /**
     * Returns the Y anchor of this <code>Sprite</code> as a percentage of its width.
     * <p>
     * Default is 0.5.
     * 
     * @return the Y anchor of this <code>Sprite</code> as a percentage of its width.
     */
    public double getAnchorY() {
        return anchorY;
    }

    /**
     * Sets the Y anchor of this <code>Sprite</code> as a percentage of its width.
     * <p>
     * The point calculated by the anchors times the size of this <code>Sprite</code> will be the origin for any rotation or scale.
     * 
     * @param anchorY
     *            the Y anchor for this <code>Sprite</code>.
     * @return this <code>Sprite</code>.
     */
    public Sprite setAnchorY(double anchorY) {
        this.anchorY = anchorY;
        set("anchorY", anchorY);
        return this;
    }

    /**
     * Sets the tint of this <code>Sprite</code> as an RGB integer.
     *      
     * @param color
     *            the tint of this <code>Sprite</code>.
     * @return this <code>Sprite</code>.
     * @exception IllegalArgumentException if color is not a valid RGB integer.
     */
    public Sprite setTint(int color) {
       requireValidColor(color);
        this.tint = color;
       set("tint", color);
        return this;
    }

    /**
     * Returns the tint of this <code>Sprite</code> as an RGB integer.
     * <p>
     * Default is 0xFFFFFF (white) 
     * 
     * @return the tint of this <code>Sprite</code>.
     */
    public int getTint() {
        return tint;
    }

}
