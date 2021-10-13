package com.codingame.gameengine.module.camera;

import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.Module;
import com.codingame.gameengine.module.entities.Entity;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The CameraModule allow you to have a dynamic camera following a set of objects
 */
@Singleton
public class CameraModule implements Module {

    GameManager<AbstractPlayer> gameManager;
    @Inject
    GraphicEntityModule entityModule;
    Map<Integer, Boolean> registered, newRegistration;
    ArrayList<Entity<?>> trackedEntities;
    Double cameraOffset;
    Integer container, sizeX, sizeY = -1;
    boolean sentContainer = false;
    double previousOffset = -1;
    boolean active = true;
    boolean oldActive = false;


    @Inject
    CameraModule(GameManager<AbstractPlayer> gameManager) {
        trackedEntities = new ArrayList<>();
        this.gameManager = gameManager;
        gameManager.registerModule(this);
        registered = new HashMap<>();
        newRegistration = new HashMap<>();
        cameraOffset = 10.;
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
        Object[] data = {null, null, null, null};
        Object[] empty = {null, null, null, null};
        if (!newRegistration.isEmpty()) {
            data[0] = new HashMap<>(newRegistration);
            newRegistration.clear();
        }
        if (cameraOffset != previousOffset) {
            data[1] = cameraOffset;
            previousOffset = cameraOffset;
        }
        if (!sentContainer && container >= 0) {
            data[2] = new Integer[]{container, sizeX, sizeY};
            sentContainer = true;
        }
        if (oldActive != active) {
            oldActive = active;
            data[3] = active;
        }
        if (!Arrays.equals(data, empty)) {
            gameManager.setViewData("c", data);
        }
    }

    private boolean isContainerChild(Entity<?> entity, int _container) {
        boolean t = false;
        Entity<?> root = entity;
        while ((!t) && root.getParent().isPresent()) {
            root = root.getParent().get();
            t = root.getId() == _container;
        }
        return t;
    }

    /**
     * Make the camera include the entity in its field of view
     *
     * @param entity the <code>Entity</code> to add to the tracked entities
     */
    public void addTrackedEntity(Entity<?> entity) {
        if (isContainerChild(entity, container)) {
            int id = entity.getId();
            trackedEntities.add(entity);
            if (!registered.getOrDefault(id, false)) {
                newRegistration.put(id, true);
                registered.put(id, true);
            }
        } else {
            throw new RuntimeException("The entity given can't be tracked because it's not a child/successor of the tracked container!\n" +
                    "Don't forget to init the camera with the setContainer method");
        }
    }

    /**
     * @param entity the <code>Entity</code> that you want to know if it's tracked
     * @return if the entity is tracked by the camera or not
     */
    public Boolean isTracked(Entity<?> entity) {
        return registered.getOrDefault(entity.getId(), false);
    }

    /**
     * Make the camera stop tracking this entity
     *
     * @param entity the <code>Entity</code> that you don't want to be tracked anymore
     */
    public void removeTrackedEntity(Entity<?> entity) {
        int id = entity.getId();
        trackedEntities.remove(entity);
        if (registered.getOrDefault(id, false)) {
            newRegistration.put(id, false);
            registered.remove(id);
        }
    }

    /**
     * Sets the camera offset to the given value. It's the length in pixel between the edge of the screen and the
     * closest to border entity
     *
     * @param value the new camera offset, a positive double
     */
    public void setCameraOffset(double value) {
        cameraOffset = value;
    }

    /**
     * Initialize the camera with container which has to contain all the other entities tracked by the camera
     *
     * @param container   the <code>Entity</code> to set as the container
     * @param viewerSizeX the x size (in pixel) in the viewer of the smallest rectangle that could include your container if the scale is 1
     * @param viewerSizeY the y size (in pixel) in the viewer of the smallest rectangle that could include your container if the scale is 1
     */
    public void setContainer(Entity<?> container, int viewerSizeX, int viewerSizeY) {
        if (trackedEntities.stream().allMatch((Entity<?> e) -> isContainerChild(e, container.getId()))) {
            this.container = container.getId();
            this.sizeX = viewerSizeX;
            this.sizeY = viewerSizeY;
        } else {
            throw new RuntimeException("You can't change the container if there are tracked that are not child of the new container");
        }
    }

}
