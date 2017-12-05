package com.codingame.gameengine.module.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldState {
    private Map<Entity<?>, EntityState> entityStateMap;
    private final double t;

    WorldState(double t) {
        this.t = t;
        entityStateMap = new HashMap<>();
    }

    Double getFrameTime() {
        return t;
    }
    
    Map<Entity<?>, EntityState> getEntityStateMap() {
        return entityStateMap;
    }

    public void flushEntityState(Entity<?> entity) {
        
        final EntityState state = entityStateMap.get(entity);
        if (state == null) {
            entityStateMap.put(entity, entity.state);
        } else {
            //TODO log warning
            entity.state.forEach((key, value) -> state.put(key, value));
        }
        entity.state = new EntityState();
    }

    public void flushMissingEntities(List<Entity<?>> entities) {
        entities.stream().forEach(entity -> {
            if (!entityStateMap.containsKey(entity)) {
                entityStateMap.put(entity, entity.state);
                entity.state = new EntityState();
            }
        });

    };
}