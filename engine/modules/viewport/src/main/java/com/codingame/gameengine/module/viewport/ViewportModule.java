package com.codingame.gameengine.module.viewport;

import java.util.HashSet;
import java.util.Set;

import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.Module;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Group;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * The ViewportModule allows you to create a zoomable/draggable container.
 * 
 * @see <a href="https://davidfig.github.io/pixi-viewport/jsdoc/">pixi-viewport</a>
 * 
 */
@Singleton
public class ViewportModule implements Module {

    GameManager<AbstractPlayer> gameManager;
    @Inject GraphicEntityModule entityModule;
    Set<Integer> registered = new HashSet<>();
    Set<Integer> newEntityIds = new HashSet<>();

    @Inject
    ViewportModule(GameManager<AbstractPlayer> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
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
        gameManager.setViewData("viewport", newEntityIds);

        newEntityIds.clear();
    }

    /**
     * Wraps the given Group in a Viewport.
     * 
     * @param group
     *            A group to wrap into a Viewport
     */
    public void createViewport(Group group) {
        int entityId = group.getId();
        if (!registered.contains(entityId)) {
            newEntityIds.add(entityId);
            registered.add(entityId);
        }
    }

}
