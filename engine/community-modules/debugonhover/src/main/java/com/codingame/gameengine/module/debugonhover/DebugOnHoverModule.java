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
 * The DebugOnHoverModule allows you to display debug entities when the mouse is over an entity. It's the
 * same as the TooltipModule, but with custom entities instead of text.
 */
@Singleton
public class DebugOnHoverModule implements Module {

    GameManager<AbstractPlayer> gameManager;
    Map<Integer, Integer[]> newRegistration;
//    Set<Entity<?>> trackedEntities; useful only if you want to allow unfollowing


    @Inject
    DebugOnHoverModule(GameManager<AbstractPlayer> gameManager) {
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


    public void setDebugHover(Entity<?> entity, Integer[] debugEntitiesIds) {
        newRegistration.put(entity.getId(), debugEntitiesIds);
    }

    public void setDebugHover(Entity<?> entity, Integer debugEntity) {
        setDebugHover(entity, new Integer[]{debugEntity});
    }

    public void setDebugHover(Entity<?> entity, Entity<?> debugEntity) {
        setDebugHover(entity, debugEntity.getId());
    }

    /**
     * Make <code>debugEntities</code> appear when the mouse is over <code>entity</code>.
     *
     * @param entity the entity to track
     * @param debugEntities the entities to display when the mouse is over <code>entity</code>
     */
    public void setDebugHover(Entity<?> entity, Entity<?>[] debugEntities) {
        setDebugHover(entity, Arrays.stream(debugEntities).map(Entity::getId).toArray(Integer[]::new));
    }

    /**
     * Remove all debugEntities of <code>entity</code>
     * @param entity the entity to stop tracking
     */
    public void stopTracking(Entity<?> entity) {
        newRegistration.put(entity.getId(), new Integer[]{});
    }


}