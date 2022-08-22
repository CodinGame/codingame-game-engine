package com.codingame.gameengine.module.interactivedisplay;

import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.Module;
import com.codingame.gameengine.module.entities.Entity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

/**
 * The InteractiveDisplayModule allows you to display entities when the mouse is over an entity or when an entity is
 * clicked.
 */
@Singleton
public class InteractiveDisplayModule implements Module {
    public static final String BOTH = "B";
    public static final String HOVER_ONLY = "H";
    public static final String CLICK_ONLY = "C";

    private static final String DISPLAY = "D";
    private static final String RESIZE = "R";

    GameManager<AbstractPlayer> gameManager;
    Map<Integer, Map<Integer, String>> newRegistration;
    Map<Integer, Map<Integer, String>> registration;


    @Inject
    InteractiveDisplayModule(GameManager<AbstractPlayer> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
        newRegistration = new HashMap<>();
        registration = new HashMap<>();
    }

    @Override
    public void onGameInit() {
        sendFrameData();
    }

    @Override
    public void onAfterGameTurn() {
        sendFrameData();
    }

    @Override
    public void onAfterOnEnd() {
    }

    private void sendFrameData() {
        if (!newRegistration.isEmpty()) {
            Object data = new HashMap[]{new HashMap<>(newRegistration)};
            newRegistration.clear();
            gameManager.setViewData("h", data);
        }
    }


    /**
     * Make <code>displayEntities</code> appear when the mouse is over <code>entity</code>.
     *
     * @param entity        the entity to track
     * @param displayEntity the entity to display when the mouse is over <code>entity</code>
     * @param mode          when the displayEntity has to be displayed (HOVER_ONLY, CLICK_ONLY or BOTH)
     */
    public void addDisplay(Entity<?> entity, Entity<?> displayEntity, String mode) {
        Map<Integer, String> displays = registration.getOrDefault(entity.getId(), new HashMap<>());
        displays.put(displayEntity.getId(), DISPLAY + "," + mode);
        registration.put(entity.getId(), displays);
        newRegistration.put(entity.getId(), displays);
    }

    /**
     * Make <code>displayEntities</code> appear when the mouse is over <code>entity</code>.
     *
     * @param entity        the entity to track
     * @param displayEntity the entity to display when the mouse is over <code>entity</code>
     */
    public void addDisplay(Entity<?> entity, Entity<?> displayEntity) {
        addDisplay(entity, displayEntity, BOTH);
    }


    /**
     * Stop displaying/resizing entities when <code>entity</code> is hovered/clicked
     *
     * @param entity the entity to stop tracking
     */
    public void untrack(Entity<?> entity) {
        newRegistration.put(entity.getId(), new HashMap<>());
    }

    /**
     * Stop transforming associatedEntity when entity is clicked/Hovered
     *
     * @param entity           the interactive entity
     * @param associatedEntity the entity that won't be transformed anymore
     */
    public void removeTransformation(Entity<?> entity, Entity<?> associatedEntity) {
        Map<Integer, String> displays = registration.getOrDefault(entity.getId(), new HashMap<>());
        if (displays.remove(associatedEntity.getId()) != null) {
            newRegistration.put(entity.getId(), displays);
        }
    }


    /**
     * Make <code>associatedEntity</code> bigger when <code>entity</code> is hovered/clicked depending on <code>mode</code>
     *
     * @param entity           the entity to track
     * @param associatedEntity the entity to resize
     * @param factor           the factor by which the associatedEntity has to be resized
     * @param mode             when the associatedEntity has to be resized (HOVER_ONLY, CLICK_ONLY or BOTH)
     */
    public void addResize(Entity<?> entity, Entity<?> associatedEntity, double factor, String mode) {
        Map<Integer, String> resizes = registration.getOrDefault(entity.getId(), new HashMap<>());
        resizes.put(associatedEntity.getId(), RESIZE + "," + mode + "," + factor);
        registration.put(entity.getId(), resizes);
        newRegistration.put(entity.getId(), resizes);
    }

    /**
     * Make <code>associatedEntity</code> <code>factor</code> times bigger when <code>entity</code> is hovered or clicked
     *
     * @param entity           the entity to track
     * @param associatedEntity the entity to resize
     * @param factor           the factor by which the associatedEntities have to be resized
     */
    public void addResize(Entity<?> entity, Entity<?> associatedEntity, double factor) {
        addResize(entity, associatedEntity, factor, BOTH);
    }

    /**
     * Make <code>entity</code> <code>factor</code> times bigger when it is hovered or clicked depending on <code>mode</code>
     *
     * @param entity the entity to resize
     * @param mode   when the entity has to be resized (HOVER_ONLY, CLICK_ONLY or BOTH)
     */
    public void addResize(Entity<?> entity, double factor, String mode) {
        addResize(entity, entity, factor, mode);
    }

    /**
     * Make <code>entity</code> <code>factor</code> times bigger when it is hovered or clicked
     *
     * @param entity the entity to resize
     */
    public void addResize(Entity<?> entity, double factor) {
        addResize(entity, entity, factor, BOTH);
    }


}