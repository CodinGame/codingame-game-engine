package com.codingame.gameengine.module.tooltip;

import java.util.HashMap;
import java.util.Map;

import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.Module;
import com.codingame.gameengine.module.entities.Entity;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * The TooltipModule takes care of displaying tooltips under the mouse cursor when an element has a linked tooltip text.
 * 
 */
@Singleton
public class TooltipModule implements Module {

    GameManager<AbstractPlayer> gameManager;
    @Inject GraphicEntityModule entityModule;
    Map<Integer, String> registered, newRegistration;

    @Inject
    TooltipModule(GameManager<AbstractPlayer> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
        registered = new HashMap<>();
        newRegistration = new HashMap<>();
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
            Object[] data = { newRegistration };

            gameManager.setViewData("tooltips", data);
            newRegistration.clear();
        }
    }

    private boolean stringEquals(String a, String b) {
        if (a == b) {
            return true;
        } else if (a != null && a.equals(b)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets a tooltip text linked to an entity
     * 
     * @param entity
     *            the <code>Entity</code> to link the tooltip to
     * @param text
     *            is the tooltip text that will be displayed when hovering over the entity
     */
    public void setTooltipText(Entity<?> entity, String text) {
        int id = entity.getId();
        if (!stringEquals(text, registered.get(id))) {
            newRegistration.put(id, text);
            registered.put(id, text);
        }
    }

    /**
     * 
     * @param entity
     *            the <code>Entity</code> to get the associated tooltip text from
     * @return the tooltip text linked to the entity
     */
    public String getTooltipText(Entity<?> entity) {
        return registered.get(entity.getId());
    }

    /**
     * Removes the tooltip text linked to the entity
     * 
     * @param entity
     *            the <code>Entity</code> to remove a tooltip from
     */
    public void removeTooltipText(Entity<?> entity) {
        newRegistration.put(entity.getId(), null);
        registered.remove(entity.getId());
    }
}
