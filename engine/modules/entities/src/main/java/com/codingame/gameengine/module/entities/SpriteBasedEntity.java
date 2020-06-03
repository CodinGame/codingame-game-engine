package com.codingame.gameengine.module.entities;

/**
 * A Sprite is a graphical entity which displays an image. That image must be loaded into the viewer's texture cache, which you can configure by
 * adding files to the <code>assets</code> folder of your game's project.
 */
public abstract class SpriteBasedEntity<T extends SpriteBasedEntity<?>> extends TextureBasedEntity<T> implements Mask {

    private String image;
    private Integer baseWidth, baseHeight;
    private ScaleMode scaleMode = ScaleMode.LINEAR;

    /**
     * Sets the image for this <code>Sprite</code>.
     * <p>
     * You must either:
     * <ul>
     * <li>use the filename of an image relative to the assets folder of the Java project.
     * <li>use the a player's nickname token.
     * </ul>
     * 
     * @param image
     *            the name of the image to use for this <code>Sprite</code>.
     * @return this <code>Sprite</code>.
     */
    public T setImage(String image) {
        this.image = image;
        set("image", image, null);
        return self();
    }

    /**
     * Sets the image base width for this <code>Sprite</code>. If not set, the image base width is the real image width.
     * 
     * @param baseWidth
     *            image width
     * @return this <code>Sprite</code>.
     */
    public T setBaseWidth(int baseWidth) {
        this.baseWidth = baseWidth;
        set("baseWidth", baseWidth, null);
        return self();
    }

    /**
     * Sets the image base height for this <code>Sprite</code>. If not set, the image base height is the real image height.
     * 
     * @param baseHeight
     *            image height
     * @return this <code>Sprite</code>.
     */
    public T setBaseHeight(int baseHeight) {
        this.baseHeight = baseHeight;
        set("baseHeight", baseHeight, null);
        return self();
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
     * Returns the image base width for this <code>Sprite</code>. If not set, the image base width is the real image width, but this will return null.
     * 
     * @return the image base width for this <code>Sprite</code>.
     */
    public Integer getBaseWidth() {
        return baseWidth;
    }

    /**
     * Returns the image base height for this <code>Sprite</code>. If not set, the image base height is the real image height, but this will return
     * null.
     * 
     * @return the image base height for this <code>Sprite</code>.
     */
    public Integer getBaseHeight() {
        return baseHeight;
    }

    /**
     * Sets the scale mode of this <code>TextureBasedEntity</code>.
     * <p>
     * Default is LINEAR.
     * </p>
     * 
     * @param scaleMode
     *            the scale mode of this <code>TextureBasedEntity</code>.
     * @return this <code>TextureBasedEntity</code>.
     */
    public T setScaleMode(ScaleMode scaleMode) {
        this.scaleMode = scaleMode;
        set("scaleMode", scaleMode);
        return self();
    }

    /**
     * Returns the scale mode of this <code>TextureBasedEntity</code>.
     * <p>
     * Default is LINEAR.
     * </p>
     * 
     * @return the scale mode of this <code>TextureBasedEntity</code>.
     */
    public ScaleMode getScaleMode() {
        return scaleMode;
    }

}
