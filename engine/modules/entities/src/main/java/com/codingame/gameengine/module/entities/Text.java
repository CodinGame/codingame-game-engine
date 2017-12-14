package com.codingame.gameengine.module.entities;

import java.util.Objects;

/**
 * Represents a label on screen.
 */
public class Text extends TextureBasedEntity<Text> {

    private String text = "";
    private int strokeColor = 0;
    private double strokeThickness = 0;
    private int fillColor = 0;
    private int fontSize = 26;
    private String fontFamily = "Lato";

    Text() {
        super();
    }

    @Override
    Entity.Type getType() {
        return Entity.Type.TEXT;
    }

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
    public Text setText(String text) {
        Objects.requireNonNull(text);
        this.text = text;
        set("text", text);
        return this;
    }

    /**
     * Returns the color of the stroke of this <code>Text</code> as an RGB integer.
     * <p>
     * Default is 0 (black).
     * 
     * @return the string of this <code>Text</code>.
     */
    public int getStrokeColor() {
        return strokeColor;
    }

    /**
     * Sets the color of the stroke of this <code>Text</code> as an RGB integer.
     * 
     * @param strokeColor
     *            the color for the stroke of this <code>Text</code>.
     * @return this <code>Text</code>.
     * @exception IllegalArgumentException
     *                if strokeColor is not a valid RGB integer.
     */
    public Text setStrokeColor(int strokeColor) {
        requireValidColor(strokeColor);
        this.strokeColor = strokeColor;
        set("strokeColor", strokeColor);
        return this;
    }

    /**
     * Returs the thickness of the stroke of this <code>Text</code> in pixels.
     * 
     * @return the thickness of the stroke of this <code>Text</code>
     */
    public double getStrokeThickness() {
        return strokeThickness;
    }

    /**
     * Returns the thickness of the stroke of this <code>Text</code> in pixels.
     * <p>
     * Default is 0 (no stroke).
     * 
     * 
     * @param strokeThickness
     *            the thickness for the stroke of this <code>Text</code>.
     * @return this <code>Text</code>
     */
    public Text setStrokeThickness(double strokeThickness) {
        this.strokeThickness = strokeThickness;
        set("strokeThickness", strokeThickness);
        return this;
    }

    /**
     * Returns the color of the fill of this <code>Text</code> as an RGB integer.
     * <p>
     * Default is 0 (black).
     * 
     * @return the color of the fill of this <code>Text</code>
     */
    public int getFillColor() {
        return fillColor;
    }

    /**
     * Sets the color of the fill of this <code>Text</code> as an RGB integer.
     * 
     * 
     * @param fillColor
     *            the color for the fill of this <code>Text</code>.
     * @return this <code>Text</code>.
     * 
     * @exception IllegalArgumentException
     *                if fillColor is not a valid RGB integer.
     */
    public Text setFillColor(int fillColor) {
        this.fillColor = fillColor;
        set("fillColor", fillColor);
        return this;
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
    public Text setFontSize(int fontSize) {
        this.fontSize = fontSize;
        set("fontSize", fontSize);
        return this;
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
    public Text setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        set("fontFamily", fontFamily);
        return this;
    }

}
