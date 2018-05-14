package com.codingame.gameengine.module.entities;

import com.google.inject.Inject;

import java.util.List;
import java.util.Optional;

public class GraphicEntitySerializer {
    @Inject
    Serializer serializer;

    public GraphicEntitySerializer() {
    }

    void dumpNewEntity(Entity<?> e, List<Object> commands) {
        commands.add(serializer.serializeCreate(e));
    }

    void dumpLoadSpriteSheet(SpriteSheetLoader spriteSheet, List<Object> commands) {
        commands.add(serializer.serializeLoadSpriteSheet(spriteSheet));
    }

    void dumpWorldStateDiff(WorldState previous, WorldState next, List<Object> commands) {
        next.getEntityStateMap()
                .forEach((entity, state) -> {
                    Optional<EntityState> prevState = Optional.ofNullable(previous.getEntityStateMap().get(entity));
                    EntityState diff = new EntityState();
                    state.forEach((key, value) -> {
                        EntityState.Param prevValue = prevState
                                .map(s -> s.get(key))
                                .orElse(null);
                        if (!value.equals(prevValue)) {
                            diff.put(key, value);
                        }
                    });

                    // Forced commits are sent even if they are empty
                    if (next.isForce() || !diff.isEmpty()) {
                        commands.add(serializer.serializeUpdate(entity, diff, next.getFrameTime()));
                    }
                });
    }
}
