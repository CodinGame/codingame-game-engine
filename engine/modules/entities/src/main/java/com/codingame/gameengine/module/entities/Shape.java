package com.codingame.gameengine.module.entities;

/**
 * A Shape represents a graphical entity with a <b>fill</b> and a <b>line</b>, both of which have an alpha and color. You may also set the line's
 * width in world units.
 * 
 * @param <T>
 *            a subclass inheriting Entity, used in order to return <b>this</b> as a T instead of a Shape.
 */
public abstract class Shape<T extends BlendableEntity<?>> extends BlendableEntity<T> implements Mask {

    private int lineColor = 0x0, lineWidth = 0, fillColor = 0xffffff;
    private double fillAlpha = 1, lineAlpha = 1;

    Shape() {
        super();
    }

    /**
     * Sets the color of the fill of this <code>Shape</code> as an RGB integer or null if the fill should not be drawn.
     * 
     * @param color
     *            the color of the fill of this <code>Shape</code>.
     * @return this <code>Shape</code>.
     * @exception IllegalArgumentException
     *                if color isn't a valid RGB integer
     */
    public T setFillColor(int color) {
        return setFillColor(color, null);
    }

    /**
     * Sets the color of the fill of this <code>Shape</code> as an RGB integer or null if the fill should not be drawn.
     * 
     * @param color
     *            the color of the fill of this <code>Shape</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Shape</code>.
     * @exception IllegalArgumentException
     *                if color isn't a valid RGB integer
     */
    public T setFillColor(int color, Curve curve) {
        requireValidColor(color);
        this.fillColor = color;
        set("fillColor", color, curve);
        return self();
    }

    /**
     * Returns the color of the fill of this <code>Shape</code> as an RGB integer.
     * <p>
     * Default is 0xFFFFFF (white).
     * </p>
     * 
     * @return the color of the fill of this <code>Shape</code>.
     */
    public Integer getFillColor() {
        return fillColor;
    }

    /**
     * Sets the alpha of the fill of this <code>Shape</code> as a percentage.
     * 
     * @param alpha
     *            the alpha of the fill of this <code>Shape</code>.
     * @return this <code>Shape</code>.
     * @exception IllegalArgumentException
     *                if alpha &lt; 0 or alpha &gt; 1
     */
    public T setFillAlpha(double alpha) {
        return setFillAlpha(alpha, null);
    }

    /**
     * Sets the alpha of the fill of this <code>Shape</code> as a percentage.
     * 
     * @param alpha
     *            the alpha of the fill of this <code>Shape</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Shape</code>.
     * @exception IllegalArgumentException
     *                if alpha &lt; 0 or alpha &gt; 1
     */
    public T setFillAlpha(double alpha, Curve curve) {
        requireValidAlpha(alpha);
        this.fillAlpha = alpha;
        set("fillAlpha", alpha, curve);
        return self();
    }

    /**
     * Returns the alpha of the fill of this <code>Shape</code> as a percentage.
     * <p>
     * Default is 1.
     * 
     * @return the alpha of the fill of this <code>Shape</code>.
     */
    public double getFillAlpha() {
        return fillAlpha;
    }

    /**
     * Sets the alpha of the border of this <code>Shape</code> as a percentage.
     * 
     * @param alpha
     *            the alpha for the border of this <code>Shape</code>.
     * @return this <code>Shape</code>.
     * @exception IllegalArgumentException
     *                if alpha &lt; 0 or alpha &gt; 1
     */
    public T setLineAlpha(double alpha) {
        return setLineAlpha(alpha, null);
    }

    /**
     * Sets the alpha of the border of this <code>Shape</code> as a percentage.
     * 
     * @param alpha
     *            the alpha for the border of this <code>Shape</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Shape</code>.
     * @exception IllegalArgumentException
     *                if alpha &lt; 0 or alpha &gt; 1
     */
    public T setLineAlpha(double alpha, Curve curve) {
        requireValidAlpha(alpha);
        this.lineAlpha = alpha;
        set("lineAlpha", alpha, curve);
        return self();
    }

    /**
     * Returns the alpha of the border of this <code>Shape</code> as a percentage.
     * <p>
     * Default is 1.
     * 
     * @return the alpha for the border of this <code>Shape</code>.
     */
    public double getLineAlpha() {
        return lineAlpha;
    }

    /**
     * Sets the width of the border of this <code>Shape</code> in world units.
     * 
     * @param lineWidth
     *            the width for the border of this <code>Shape</code>.
     * @return this <code>Shape</code>.
     */
    public T setLineWidth(int lineWidth) {
        return setLineWidth(lineWidth, null);
    }

    /**
     * Sets the width of the border of this <code>Shape</code> in world units.
     * <p>
     * Default is 0.
     * </p>
     * 
     * @param lineWidth
     *            the width for the border of this <code>Shape</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Shape</code>.
     */
    public T setLineWidth(int lineWidth, Curve curve) {
        this.lineWidth = lineWidth;
        set("lineWidth", lineWidth, curve);
        return self();
    }

    /**
     * Returns the width of the border of this <code>Shape</code> in world units.
     * <p>
     * Default is 1.
     * 
     * @return the width of the border of this <code>Shape</code>.
     */
    public int getLineWidth() {
        return lineWidth;
    }

    /**
     * Sets the color of the border of this <code>Shape</code> as an RGB integer.
     * 
     * @param lineColor
     *            the color for the border of this <code>Shape</code>.
     * @return this <code>Shape</code>.
     * @exception IllegalArgumentException
     *                if lineColor isn't a valid RGB integer
     */
    public T setLineColor(int lineColor) {
        return setLineColor(lineColor, null);
    }

    /**
     * Sets the color of the border of this <code>Shape</code> as an RGB integer.
     * 
     * @param lineColor
     *            the color for the border of this <code>Shape</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Shape</code>.
     * @exception IllegalArgumentException
     *                if lineColor isn't a valid RGB integer
     */
    public T setLineColor(int lineColor, Curve curve) {
        requireValidColor(lineColor);
        this.lineColor = lineColor;
        set("lineColor", lineColor, curve);
        return self();
    }

    /**
     * Returns the color of the border of this <code>Shape</code> as an RGB integer.
     * <p>
     * Default is 0xFFFFFF (white).
     * 
     * @return the color of the border of this <code>Shape</code>.
     */
    public int getLineColor() {
        return lineColor;
    }

}