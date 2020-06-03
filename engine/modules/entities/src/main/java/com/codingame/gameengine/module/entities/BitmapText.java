package com.codingame.gameengine.module.entities;

import java.util.Objects;

/**
 * Represents a label on screen, you can use any bitmap font in your asset folder as it's font.
 */
public class BitmapText extends TextBasedEntity<BitmapText> {

    private String font;

    @Override
    Type getType() {
        return Entity.Type.BITMAPTEXT;
    }

    /**
     * Returns the name of the font of this <code>BitmapText</code> in px.
     * <p>
     * Default is null.
     * </p>
     * 
     * @return the size of the font of this <code>BitmapText</code>.
     */
    public String getFont() {
        return font;
    }

    /**
     * Sets the name of the font of this <code>BitmapText</code>.
     * <p>
     * Only fonts available to the browser can be displayed.
     * </p>
     * <p>
     * Default is null.
     * </p>
     * 
     * 
     * @param font
     *            the size for the font of this <code>BitmapText</code>.
     * @return this <code>BitmapText</code>.
     * @exception NullPointerException
     *                if font is null.
     */
    public BitmapText setFont(String font) {
        Objects.requireNonNull(font);
        this.font = font;
        set("fontFamily", font, null);
        return this;
    }

}
