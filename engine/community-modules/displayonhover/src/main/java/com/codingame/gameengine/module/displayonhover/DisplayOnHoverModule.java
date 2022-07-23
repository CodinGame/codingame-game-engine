package view.modules;

import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.Module;
import com.codingame.gameengine.module.entities.Entity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The DisplayOnHoverModule allows you to display entities when the mouse is over an entity. It's the
 * same as the TooltipModule, but with custom entities instead of text.
 */
@Singleton
public class DisplayOnHoverModule implements Module {

    GameManager<AbstractPlayer> gameManager;
    Map<Integer, Integer[]> newRegistration;


    @Inject
    DisplayOnHoverModule(GameManager<AbstractPlayer> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
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
            Object data = new HashMap[]{new HashMap<>(newRegistration)};
            newRegistration.clear();
            gameManager.setViewData("h", data);
        }
    }


    /**
     * Make <code>displayEntities</code> appear when the mouse is over <code>entity</code>.
     *
     * @param entity the entity to track
     * @param displayEntity the entity to display when the mouse is over <code>entity</code>
     */
    public void setDisplayHover(Entity<?> entity, Entity<?> displayEntity) {
        setDisplayHover(entity.getId(), new Integer[](displayEntity.getId()));
    }

    /**
     * Make <code>displayEntities</code> appear when the mouse is over <code>entity</code>.
     *
     * @param entity the entity to track
     * @param displayEntities the entities to display when the mouse is over <code>entity</code>
     */
    public void setDisplayHover(Entity<?> entity, Entity<?>[] displayEntities) {
        setDisplayHover(entity, Arrays.stream(displayEntities).map(Entity::getId).toArray(Integer[]::new));
    }

    /**
     * Stop displaying entities when <code>entity</code> is hovered
     * @param entity the entity to stop tracking
     */
    public void untrack(Entity<?> entity) {
        newRegistration.put(entity.getId(), new Integer[]{});
    }


}