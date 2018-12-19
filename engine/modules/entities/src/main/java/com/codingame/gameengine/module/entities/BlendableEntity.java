package com.codingame.gameengine.module.entities;

public abstract class BlendableEntity<T extends Entity<?>> extends Entity<T> {
    /**
     * The list of supported PIXI blend modes and their associated constant.
     * 
     * @see <a href="http://pixijs.download/dev/docs/PIXI.html#.BLEND_MODES">PIXI BLEND_MODES</a>
     */
    public static enum BlendMode {
        NORMAL(0), ADD(1), MULTIPLY(2), SCREEN(3);
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
     * Sets the blend mode for this <code>TextureBasedEntity</code>.
     * <p>
     * The possible values are found in <code>BlendMode</code>.
     * 
     * @see <a href="http://pixijs.download/dev/docs/PIXI.html#.BLEND_MODES">PIXI BLEND_MODES</a>
     * @param blendMode
     * @return this <code>TextureBasedEntity</code>.
     */
    public T setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
        set("blendMode", blendMode.getValue(), null);
        return self();
    }
}
