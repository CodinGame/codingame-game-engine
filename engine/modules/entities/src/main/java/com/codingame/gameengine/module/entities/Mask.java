package com.codingame.gameengine.module.entities;

/**
 * <p>
 * Entities that implement this interface can be used as a <code>Mask</code> by other entities.
 * </p>
 * <code>Masks</code> can only be assigned to <b>one</b> entity at a time.
 *
 */
public interface Mask {
    /**
     * Returns a unique <code>Entity</code> identifier for this <code>Mask</code>.
     * 
     * @return A unique identifier.
     */
    public int getId();
}
