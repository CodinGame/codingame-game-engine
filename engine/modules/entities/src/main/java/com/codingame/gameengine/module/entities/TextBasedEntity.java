package com.codingame.gameengine.module.entities;

import java.util.Objects;

public abstract class TextBasedEntity<T extends TextureBasedEntity<?>> extends TextureBasedEntity<T> {
    protected String text = "";
    protected int fontSize = 26;
    protected String fontFamily = "Lato";
    
    /**
     * Returns the string this <code>Text</code> displays.
     * <p>
     * Default is "" (empty string).
     * 
     * @return the string of this <code>Text</code>.
     */
    public String getText() {
        return text;
    }
    

    /**
     * Sets the string for this <code>Text</code> to display.
     * 
     * @param text
     *            the string for this <code>Text</code> to display.
     * @return this <code>Text</code>.
     * @exception NullPointerException
     *                if text is null.
     */
    public T setText(String text) {
        Objects.requireNonNull(text);
        this.text = text;
        set("text", text, null);
        return self();
    }
    
    /**
     * Returns the size of the font of this <code>Text</code> in px.
     * <p>
     * Default is 26.
     * 
     * @return the size of the font of this <code>Text</code>.
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Sets the size of the font of this <code>Text</code> in px.
     * 
     * 
     * @param fontSize
     *            the size for the font sof this <code>Text</code>.
     * @return this <code>Text</code>.
     */
    public T setFontSize(int fontSize) {
        return setFontSize(fontSize, null);
    }

    /**
     * Sets the size of the font of this <code>Text</code> in px.
     * 
     * 
     * @param fontSize
     *            the size for the font sof this <code>Text</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Text</code>.
     */
    public T setFontSize(int fontSize, Curve curve) {
        this.fontSize = fontSize;
        set("fontSize", fontSize, curve);
        return self();
    }

    /**
     * Returns the name of the font of this <code>Text</code> in px.
     * <p>
     * Default is "Lato".
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
     *            the size for the font sof this <code>Text</code>.
     * @return this <code>Text</code>.
     */
    public T setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        set("fontFamily", fontFamily, null);
        return self();
    }


}
