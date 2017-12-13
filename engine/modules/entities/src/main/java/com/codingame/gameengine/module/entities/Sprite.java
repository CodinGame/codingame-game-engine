package com.codingame.gameengine.module.entities;

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
}
