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
    public static final int BOTH = 3;
    public static final int HOVER_ONLY = 2;
    public static final int CLICK_ONLY = 1;
    GameManager<AbstractPlayer> gameManager;
    Map<Integer, Map<Integer, Integer>> newRegistration;
    Map<Integer, Map<Integer, Integer>> registration;


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
    public void addDisplay(Entity<?> entity, Entity<?> displayEntity, int mode) {
        Map<Integer, Integer> displays = registration.getOrDefault(entity.getId(), new HashMap<>());
        displays.put(displayEntity.getId(), mode);
        registration.put(entity.getId(), displays);
        newRegistration.put(entity.getId(), displays);
    }

    /**
     * Make <code>displayEntities</code> appear when the mouse is over <code>entity</code>.
     *
     * @param entity          the entity to track
     * @param displayEntities the entities to display when the mouse is over <code>entity</code>
     */
    public void addDisplay(Entity<?> entity, Entity<?>[] displayEntities, int mode) {
        for (Entity<?> displayEntity : displayEntities) {
            addDisplay(entity, displayEntity, mode);
        }
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
     * Make <code>displayEntities</code> appear when the mouse is over <code>entity</code>.
     *
     * @param entity          the entity to track
     * @param displayEntities the entities to display when the mouse is over <code>entity</code>
     */
    public void addDisplay(Entity<?> entity, Entity<?>[] displayEntities) {
        addDisplay(entity, displayEntities, BOTH);
    }

    /**
     * Stop displaying entities when <code>entity</code> is hovered
     *
     * @param entity the entity to stop tracking
     */
    public void untrack(Entity<?> entity) {
        newRegistration.put(entity.getId(), new HashMap<>());
    }

    /**
     * Stop displaying displayEntity when entity is clicked/Hovered
     *
     * @param entity        the interactive entity
     * @param displayEntity the entity that won't be displayed anymore
     */
    public void removeDisplay(Entity<?> entity, Entity<?> displayEntity) {
        Map<Integer, Integer> displays = registration.getOrDefault(entity.getId(), new HashMap<>());
        if (displays.remove(displayEntity.getId()) != null) {
            newRegistration.put(entity.getId(), displays);
        }
    }


}