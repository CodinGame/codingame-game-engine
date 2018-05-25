package com.codingame.gameengine.module.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


class WorldState {
    private Map<Entity<?>, EntityState> entityStateMap;
    private final String t;
    private boolean autocommit, force;

    WorldState(String t) {
        this(t, false);
    }

    public WorldState(String t, boolean autocommit) {
        this.t = t;
        entityStateMap = new HashMap<>();
        this.autocommit = autocommit;
    }

    String getFrameTime() {
        return t;
    }

    Map<Entity<?>, EntityState> getEntityStateMap() {
        return entityStateMap;
    }

    /**
     * Performs a flush of all the entity states that are not already present in the state map.
     * This allows the default behaviour of commiting all entities at t = 1, which can be overriden.
     */
    void flushMissingEntities(List<Entity<?>> entities) {
        entities.stream().forEach(entity -> {
            if (!entityStateMap.containsKey(entity)) {
                entityStateMap.put(entity, entity.state);
                entity.state = new EntityState();
            }
            
        });

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

    public boolean isAutocommit() {
        return autocommit;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    };

    public WorldState diffFromOtherWorldState(WorldState previousWorldState) {
//        List<Object> newCommands = new ArrayList<>();
        
        WorldState worldDiff = new WorldState(this.t);
        
        getEntityStateMap()
                .forEach((entity, nextEntityState) -> {
                    Optional<EntityState> prevEntityState = Optional.ofNullable(previousWorldState.getEntityStateMap().get(entity));
                    EntityState entitiesDiff = nextEntityState.diffFromOtherState(prevEntityState);

                    // Forced commits are sent even if they are empty
                    if (isForce() || !entitiesDiff.isEmpty()) {
//                        String serializedStateDiff = serializer.serializeEntitiesStateDiff(entity, entitiesDiff, getFrameTime());
//                        newCommands.add(serializedStateDiff);
                        worldDiff.getEntityStateMap().put(entity, entitiesDiff);
                    }
                });
        return worldDiff;
    }
}
