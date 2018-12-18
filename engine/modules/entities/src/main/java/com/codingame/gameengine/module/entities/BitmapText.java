package com.codingame.gameengine.module.entities;

/**
 * Represents a label on screen, you can use any bitmap font in your asset folder as it's font.
 */
public class BitmapText extends TextBasedEntity<BitmapText> {

    private String fontFamily = "";
    
    @Override
    Type getType() {
        return Entity.Type.BITMAPTEXT;
    }

    /**
     * Returns the name of the font of this <code>Text</code> in px.
     * <p>
     * Default is an empty string.
     * 
     * @return the size of the font of this <code>Text</code>.
     */
    public String getFontFamily() {
        return fontFamily;
    }

    /**
     * Sets the name of the font of this <code>Text</code>.
     * <p>
     * Only fonts available to the browser can be displayed.
     * 
     * 
     * @param fontFamily
     *            the size for the font of this <code>Text</code>.
     * @return this <code>Text</code>.
     */
    public BitmapText setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        set("fontFamily", fontFamily, null);
        return this;
    }

}
