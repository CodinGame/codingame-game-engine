package com.codingame.gameengine.module.entities;

/**
 * Any PIXI Entity that can be attributed a <code>BlendMode</code>.
 *
 * @param <T> a subclass inheriting Entity, used in order to return <b>this</b> as a T instead of a <code>BlendableEntity</code>.
 */
public abstract class BlendableEntity<T extends Entity<?>> extends Entity<T> {
    /**
     * The list of supported PIXI blend modes and their associated constant.
     * 
     * @see <a href="http://pixijs.download/dev/docs/PIXI.html#.BLEND_MODES">PIXI BLEND_MODES</a>
     */
    public static enum BlendMode {
        /**
         * No pixel blend, only the values of the top layer are kept. 
         */
        NORMAL(0), 
        /**
         * Adds pixel values of one layer with the other. 
         */
        ADD(1),
        /**
         * Multiplies the numbers for each pixel of the top layer with the corresponding pixel for the bottom layer. The result is a darker picture.
         */
        MULTIPLY(2),
        /**
         * The values of the pixels in the two layers are inverted, multiplied, and then inverted again. The result is a brighter picture.
         */
        SCREEN(3);
        private int value;

        private BlendMode(int value) {
            this.value = value;
        }

        private int getValue() {
            return value;
        }
    }

    private BlendMode blendMode;
    
    /**
     * Returns the <code>BlendMode</code> this <code>TextureBasedEntity</code> is to be drawn with.
     * 
     * @see <a href="http://pixijs.download/dev/docs/PIXI.html#.BLEND_MODES">PIXI BLEND_MODES</a>
     * @return the <code>BlendMode</code> this <code>TextureBasedEntity</code> is to be drawn with.
     */
    public BlendMode getBlendMode() {
        return blendMode;
    }

    /**
     * <p>
     * Sets the blend mode for this <code>TextureBasedEntity</code>.
     * </p>
     * The possible values are found in <code>BlendMode</code>.
     * 
     * @see <a href="http://pixijs.download/dev/docs/PIXI.html#.BLEND_MODES">PIXI BLEND_MODES</a>
     * @param blendMode the <code>BlendMode</code> to use.
     * @return this <code>TextureBasedEntity</code>.
     */
    public T setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
        set("blendMode", blendMode.getValue(), null);
        return self();
    }
}
