package com.codingame.gameengine.module.entities;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.inject.Inject;

/**
 * A <code>SpriteAnimation</code> is a graphical entity which displays a sequence of images. You can use this class to animate a sprite based on
 * milliseconds, rather than frame time.
 */
public class SpriteAnimation extends TextureBasedEntity<SpriteAnimation> {

    @Inject GraphicEntityModule entityModule;

    private int START_INDEX = 0;

    private String[] images = new String[] {};

    private boolean loop;
    private boolean started;
    private int duration = 1000;

    SpriteAnimation() {
        super();
    }

    @Override
    Entity.Type getType() {
        return Entity.Type.SPRITEANIMATION;
    }

    private static void requireValidDuration(int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("Invalid duration: " + duration + "ms");
        }
    }

    /**
     * Returns whether the animation is flagged to have the graphical counterpart start animating if it hasn't yet.
     * <p>
     * Default is false.
     * </p>
     * 
     * @return true if the animation has been started.
     * 
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Begins or restarts the animation, it will always begin at the first image.
     * <p>
     * Setting this to false will stop the animation, but it may not be resumed without restarting.
     * </p>
     * 
     * @param started
     *            true to begin or restart animation, false to stop animation
     * @return this animation.
     */
    public SpriteAnimation setStarted(boolean started) {
        this.started = started;

        if (started) {
            set("started", START_INDEX, null);
        } else {
            set("started", "", null);
        }
        return this;
    }

    /**
     * Calls setStarted(true) and forces the animation to play from the start;
     * 
     * @return this animation.
     */
    public SpriteAnimation reset() {
        START_INDEX++;
        return setStarted(true);
    }

    /**
     * Calls setStarted(true);
     * 
     * @return this animation.
     */
    public SpriteAnimation start() {
        return setStarted(true);
    }

    /**
     * Calls setStarted(false);
     * 
     * @return this animation.
     */
    public SpriteAnimation stop() {
        return setStarted(false);
    }

    /**
     * Returns whether the animation should loop.
     * <p>
     * Default is false.
     * </p>
     * 
     * @return true if the animation loops.
     * 
     */
    public boolean isLoop() {
        return loop;
    }

    /**
     * Sets whether the animation loops. If false, the last image of the animation stays visible at the end.
     * 
     * @param loop
     *            true to make this animation loop.
     * @return this animation.
     */
    public SpriteAnimation setLoop(boolean loop) {
        this.loop = loop;
        set("loop", loop, null);
        return this;
    }

    /**
     * Returns the duration of the animation in milliseconds.
     * <p>
     * Default is 1000.
     * </p>
     * 
     * @return the duration of the animation in milliseconds.
     * 
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the duration of the animation in milliseconds.
     * 
     * @param duration
     *            the duration of the animation in milliseconds.
     * @return this animation.
     * @throws IllegalArgumentException
     *             if duration is negative.
     */
    public SpriteAnimation setDuration(int duration) {
        requireValidDuration(duration);
        this.duration = duration;
        set("duration", duration, null);
        return this;
    }

    /**
     * Sets the sequence of images for this animation.
     * 
     * @param images
     *            the names of the images to use for this animation.
     * @return this animation.
     * @throws IllegalArgumentException
     *             if images is empty.
     */
    public SpriteAnimation setImages(String... images) {
        if (images.length == 0) {
            throw new IllegalArgumentException("Animation must contain at least 1 image.");
        }
        this.images = images;
        set("images", Stream.of(images).collect(Collectors.joining(",")), null);
        return this;
    }

    /**
     * Returns the names of the images used for this animation.
     * 
     * @return the names of the images used for this animation.
     */
    public String[] getImages() {
        return images;
    }
}