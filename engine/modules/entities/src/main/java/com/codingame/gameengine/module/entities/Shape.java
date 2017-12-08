package com.codingame.gameengine.module.entities;

abstract public class Shape<T extends Entity<?>> extends Entity<T> {

    private Integer fillColor = null;
    private int lineColor = 0xffffff, lineWidth = 1;
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
     * @exception IllegalArgumentException if color isn't a valid RGB integer
     */
    public T setFillColor(int color) {
        requireValidColor(color);
        this.fillColor = color;
        set("fillColor", color);
        return self();
    }

    /**
     * Returns the color of the fill of this <code>Shape</code> as an RGB integer.
     * <p>
     * Can be null if no fill should be drawn.
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
        requireValidAlpha(alpha);
        this.fillAlpha = alpha;
        set("fillAlpha", alpha);
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
        requireValidAlpha(alpha);
        this.lineAlpha = alpha;
        set("lineAlpha", alpha);
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
        this.lineWidth = lineWidth;
        set("lineWidth", lineWidth);
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
     * @exception IllegalArgumentException if lineColor isn't a valid RGB integer
     */
    public T setLineColor(int lineColor) {
        requireValidColor(lineColor);
        this.lineColor = lineColor;
        set("lineColor", lineColor);
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