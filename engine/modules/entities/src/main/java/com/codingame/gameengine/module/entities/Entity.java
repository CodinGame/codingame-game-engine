package com.codingame.gameengine.module.entities;

import java.util.Optional;

/**
 * A graphical entity, displayed on screen in the game's replay.
 * <p>
 * </p>
 * The graphical counterpart's coordinates are converted from world units to pixel coordinates.
 * 
 * @param <T>
 *            a subclass inheriting Entity, used in order to return <b>this</b> as a T instead of an Entity.
 */
public abstract class Entity<T extends Entity<?>> {
    final int id;
    EntityState state;

    private int x, y, zIndex;
    private double scaleX = 1, scaleY = 1;
    private boolean visible = true;
    private double rotation, alpha = 1;
    Group parent;

    static enum Type {
        CIRCLE, LINE, RECTANGLE, SPRITE, TEXT, GROUP, SPRITEANIMATION
    }

    Entity() {
        id = ++GraphicEntityModule.ENTITY_COUNT;
        state = new EntityState();

    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    int getId() {
        return id;
    }

    protected void set(String key, Object value, Curve curve) {
        state.put(key, value, Optional.ofNullable(curve).orElse(Curve.DEFAULT));
    }

    abstract Type getType();

    /**
     * Sets the X coordinate of this <code>Entity</code> in world units.
     * 
     * @param x
     *            the X coordinate for this <code>Entity</code>.
     * @return this <code>Entity</code>.
     */
    public T setX(int x) {
        return setX(x, null);
    }

    /**
     * Sets the X coordinate of this <code>Entity</code> in world units.
     * 
     * @param x
     *            the X coordinate for this <code>Entity</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Entity</code>.
     */
    public T setX(int x, Curve curve) {
        this.x = x;
        set("x", x, curve);
        return self();
    }

    /**
     * Sets the Y coordinate of this <code>Entity</code> in world units.
     * 
     * @param y
     *            the Y coordinate for this <code>Entity</code>.
     * @return this <code>Entity</code>.
     */
    public T setY(int y) {
        return setY(y, null);
    }

    /**
     * Sets the Y coordinate of this <code>Entity</code> in world units.
     * 
     * @param y
     *            the Y coordinate for this <code>Entity</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Entity</code>.
     */
    public T setY(int y, Curve curve) {
        this.y = y;
        set("y", y, curve);
        return self();
    }

    /**
     * Sets the z-index of this <code>Entity</code> used to compute the display order for overlapping entities.
     * <p>
     * An <code>Entity</code> with a higher z-index is displayed over one with a smaller z-index.
     * <p>
     * In case of equal values, the most recently created <code>Entity</code> will be on top.
     * 
     * @param zIndex
     *            the z-index for this <code>Entity</code>.
     * @return this <code>Entity</code>.
     */
    public T setZIndex(int zIndex) {
        this.zIndex = zIndex;
        set("zIndex", zIndex, null);
        return self();
    }

    /**
     * Sets the horizontal scale of this <code>Entity</code> as a percentage.
     * 
     * @param scaleX
     *            the horizontal scale for this <code>Entity</code>.
     * @return this <code>Entity</code>.
     */
    public T setScaleX(double scaleX) {
        return setScaleX(scaleX, null);
    }

    /**
     * Sets the horizontal scale of this <code>Entity</code> as a percentage.
     * 
     * @param scaleX
     *            the horizontal scale for this <code>Entity</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Entity</code>.
     */
    public T setScaleX(double scaleX, Curve curve) {
        this.scaleX = scaleX;
        set("scaleX", scaleX, curve);
        return self();
    }

    /**
     * Sets the vertical scale of this <code>Entity</code> as a percentage.
     * 
     * @param scaleY
     *            the vertical scale for this <code>Entity</code>.
     * @return this <code>Entity</code>.
     */
    public T setScaleY(double scaleY) {
        return setScaleY(scaleY, null);
    }

    /**
     * Sets the vertical scale of this <code>Entity</code> as a percentage.
     * 
     * @param scaleY
     *            the vertical scale for this <code>Entity</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Entity</code>.
     */
    public T setScaleY(double scaleY, Curve curve) {
        this.scaleY = scaleY;
        set("scaleY", scaleY, curve);
        return self();
    }

    /**
     * Sets the alpha of this <code>Entity</code> as a percentage.
     * <p>
     * 1 is opaque and 0 is invisible.
     * 
     * @param alpha
     *            the alpha for this <code>Entity</code>.
     * @exception IllegalArgumentException
     *                if alpha &lt; 0 or alpha &gt; 1
     * @return this <code>Entity</code>.
     */
    public T setAlpha(double alpha) {
        return setAlpha(alpha, null);
    }

    /**
     * Sets the alpha of this <code>Entity</code> as a percentage.
     * <p>
     * 1 is opaque and 0 is invisible.
     * 
     * @param alpha
     *            the alpha for this <code>Entity</code>.
     * 
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Entity</code>.
     * @exception IllegalArgumentException
     *                if alpha &lt; 0 or alpha &gt; 1
     */
    public T setAlpha(double alpha, Curve curve) {
        requireValidAlpha(alpha);

        this.alpha = alpha;
        set("alpha", alpha, curve);
        return self();
    }

    /**
     * Sets both the horizontal and vertical scale of this <code>Entity</code> to the same percentage.
     * 
     * @param scale
     *            the scale for this <code>Entity</code>.
     * @return this <code>Entity</code>.
     */
    public T setScale(double scale) {
        return setScale(scale, null);
    }

    /**
     * Sets both the horizontal and vertical scale of this <code>Entity</code> to the same percentage.
     * 
     * @param scale
     *            the scale for this <code>Entity</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Entity</code>.
     */
    public T setScale(double scale, Curve curve) {
        setScaleX(scale, curve);
        setScaleY(scale, curve);
        return self();
    }

    /**
     * Sets the rotation of this <code>Entity</code> in radians.
     * 
     * @param rotation
     *            the rotation for this <code>Entity</code>.
     * @return this <code>Entity</code>.
     */
    public T setRotation(double rotation) {
        return setRotation(rotation, null);
    }

    /**
     * Sets the rotation of this <code>Entity</code> in radians.
     * 
     * @param rotation
     *            the rotation for this <code>Entity</code>.
     * @param curve
     *            the transition to animate between values of this property.
     * @return this <code>Entity</code>.
     */
    public T setRotation(double rotation, Curve curve) {
        this.rotation = rotation;
        set("rotation", rotation, curve);
        return self();
    }

    /**
     * Flags this <code>Entity</code> to be drawn on screen or not.
     * <p>
     * Default is true.
     * 
     * @param visible
     *            the value for this <code>Entity</code>'s visible flag.
     * @return this <code>Entity</code>.
     */
    public T setVisible(boolean visible) {
        this.visible = visible;
        set("visible", visible, null);
        return self();
    }

    /**
     * Returns the X coordinate of this <code>Entity</code> in world units.
     * 
     * @return the X coordinate of this <code>Entity</code>.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the Y coordinate of this <code>Entity</code> in world units.
     * 
     * @return the Y coordinate of this <code>Entity</code>.
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the z-index of this <code>Entity</code> used to compute the display order for overlapping entities.
     * 
     * @return the z-index of this <code>Entity</code>.
     */
    public int getZIndex() {
        return zIndex;
    }

    /**
     * Returns the horizontal scale of this <code>Entity</code> as a percentage.
     * <p>
     * Default is 1.
     * 
     * @return the horizontal scale of this <code>Entity</code>.
     */
    public double getScaleX() {
        return scaleX;
    }

    /**
     * Returns the vertical scale of this <code>Entity</code> as a percentage.
     * <p>
     * Default is 1.
     * 
     * @return the vertical scale of this <code>Entity</code>.
     */
    public double getScaleY() {
        return scaleY;
    }

    /**
     * Returns the alpha of this <code>Entity</code> as a percentage.
     * <p>
     * Default is 1.
     * 
     * @return the alpha of this <code>Entity</code>.
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Returns the rotation of this <code>Entity</code> in radians.
     * 
     * @return the rotation coordinate of this <code>Entity</code>.
     */
    public double getRotation() {
        return rotation;
    }

    /**
     * Returns whether this <code>Entity</code> is flagged to be drawn on screen.
     * 
     * @return the value of the visible flag of this <code>Entity</code>.
     */
    public boolean isVisible() {
        return visible;
    }

    protected static void requireValidAlpha(double alpha) {
        if (alpha > 1) {
            throw new IllegalArgumentException("An alpha may not exceed 1");
        } else if (alpha < 0) {
            throw new IllegalArgumentException("An alpha may not be less than 0");
        }
    }

    protected static void requireValidColor(int color) {
        if (color > 0xFFFFFF) {
            throw new IllegalArgumentException(color + "is not a valid RGB integer.");
        }
    }
}