package com.codingame.gameengine.module.entities;

/**
 * A Sprite is a graphical entity which displays an image. That image must be loaded into the viewer's texture cache, which you can configure by
 * adding files to the <code>assets</code> folder of your game's project.
 */
public class Sprite extends TextureBasedEntity<Sprite> {

    private String image;

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
     * <li>use the filename of an image relative to the assets folder of the Java project.
     * <li>use the a player's nickname token.
     * </ul>
     * 
     * @param image
     *            the name of the image to use for this <code>Sprite</code>.
     * @return this <code>Sprite</code>.
     */
    public Sprite setImage(String image) {
        return setImage(image, null);
    }

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
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Sprite</code>.
     */
    public Sprite setImage(String image, Curve curve) {
        this.image = image;
        set("image", image, curve);
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
}