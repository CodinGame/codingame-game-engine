package com.codingame.gameengine.module.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class WorldState {
    private Map<Entity<?>, EntityState> entityStateMap;
    private final String t;
    private boolean worldCommit = false;

    WorldState(String t) {
        this.t = t;
        entityStateMap = new HashMap<>();
    }

    String getFrameTime() {
        return t;
    }

    Map<Entity<?>, EntityState> getEntityStateMap() {
        return entityStateMap;
    }

    /**
     * Performs a flush of all the entity states that are not already present in the state map. This allows the default behaviour of commiting all
     * entities at t = 1, which can be overridden.
     */
    void flushMissingEntities(List<Entity<?>> entities) {
        entities.stream().forEach(entity -> {
            if (!entityStateMap.containsKey(entity)) {
                entityStateMap.put(entity, entity.state);
                entity.state = new EntityState();
            }

        });

    }

    void markAsWorldCommit() {
        this.worldCommit = true;
    }

    boolean isWorldCommit() {
        return worldCommit;
    }

    private void updateStateMap(EntityState oldState, EntityState currentState, Entity<?> entity) {
        if (oldState == null) {
            entityStateMap.put(entity, currentState);
        } else {
            currentState.forEach((key, value) -> oldState.put(key, value));
        }
    }

    void flushEntityState(Entity<?> entity) {
        final EntityState state = entityStateMap.get(entity);
        updateStateMap(state, entity.state, entity);
        entity.state = new EntityState();
    }

    void updateAllEntities(WorldState next) {
        next.entityStateMap.forEach((entity, nextState) -> {
            EntityState oldState = entityStateMap.get(entity);
            updateStateMap(oldState, nextState, entity);
        });
    }

    public WorldState diffFromOtherWorldState(WorldState previousWorldState) {
        WorldState worldDiff = new WorldState(this.t);

        getEntityStateMap()
            .forEach((entity, nextEntityState) -> {
                Optional<EntityState> prevEntityState = Optional.ofNullable(previousWorldState.getEntityStateMap().get(entity));
                EntityState entitiesDiff = nextEntityState.diffFromOtherState(prevEntityState);

                // Forced entities should be sent even if they are empty
                if (!isWorldCommit() || !entitiesDiff.isEmpty()) {
                    worldDiff.getEntityStateMap().put(entity, entitiesDiff);
                }
            });
        return worldDiff;
    }
}
