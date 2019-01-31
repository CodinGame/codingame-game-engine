package com.codingame.gameengine.module.entities;

/**
 * Represents a label on screen.
 */
public class Text extends TextBasedEntity<Text> {

    /**
     * The list of supported font weights. 
     */
    @SuppressWarnings("javadoc")
    public static enum FontWeight {
        NORMAL, BOLD, BOLDER, LIGHTER;

        public String toString() {
            return name().toLowerCase();
        }
    }

    private int strokeColor = 0;
    private double strokeThickness = 0;
    private int fillColor = 0;
    private String fontFamily = "Lato";
    private FontWeight fontWeight = FontWeight.NORMAL;

    Text() {
        super();
    }

    @Override
    Entity.Type getType() {
        return Entity.Type.TEXT;
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
        return setStrokeColor(strokeColor, null);
    }

    /**
     * Sets the color of the stroke of this <code>Text</code> as an RGB integer.
     * 
     * @param strokeColor
     *            the color for the stroke of this <code>Text</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Text</code>.
     * @exception IllegalArgumentException
     *                if strokeColor is not a valid RGB integer.
     */
    public Text setStrokeColor(int strokeColor, Curve curve) {
        requireValidColor(strokeColor);
        this.strokeColor = strokeColor;
        set("strokeColor", strokeColor, curve);
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
     *            the size for the font of this <code>Text</code>.
     * @return this <code>Text</code>.
     */
    public Text setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        set("fontFamily", fontFamily, null);
        return this;
    }

    /**
     * Returns the thickness of the stroke of this <code>Text</code> in pixels.
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
        return setStrokeThickness(strokeThickness, null);
    }

    /**
     * Returns the thickness of the stroke of this <code>Text</code> in pixels.
     * <p>
     * Default is 0 (no stroke).
     * 
     * 
     * @param strokeThickness
     *            the thickness for the stroke of this <code>Text</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Text</code>
     */
    public Text setStrokeThickness(double strokeThickness, Curve curve) {
        this.strokeThickness = strokeThickness;
        set("strokeThickness", strokeThickness, curve);
        return this;
    }
    /**
     * Sets the weight of the font of this <code>Text</code>.
     * 
     * @param weight
     *            the FontWeight of the <code>Text</code>.
     * @return this <code>Text</code>.
     */
    public Text setFontWeight(FontWeight weight) {
        this.fontWeight = weight;
        set("fontWeight", weight.toString());
        return this;
    }

    /**
     * Returns the weight of the font of this <code>Text</code>.
     * <p>
     * Default is NORMAL.
     * 
     * @return the weight of the font of this <code>Text</code>.
     */
    public FontWeight getFontWeight() {
        return this.fontWeight;
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
        return setFillColor(fillColor, null);
    }

    /**
     * Sets the color of the fill of this <code>Text</code> as an RGB integer.
     * 
     * 
     * @param fillColor
     *            the color for the fill of this <code>Text</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Text</code>.
     * 
     * @exception IllegalArgumentException
     *                if fillColor is not a valid RGB integer.
     */
    public Text setFillColor(int fillColor, Curve curve) {
        this.fillColor = fillColor;
        set("fillColor", fillColor, curve);
        return this;
    }
}
