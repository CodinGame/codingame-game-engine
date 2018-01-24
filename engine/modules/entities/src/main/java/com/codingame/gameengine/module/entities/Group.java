package com.codingame.gameengine.module.entities;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Group is an Entity which acts as a container for other entities.
 */
public class Group extends Entity<Group> {

    private Set<Entity<?>> entities;

    Group() {
        super();
        entities = new HashSet<>();
    }

    /**
     * Separates the given entity from this <code>Group</code>.
     * 
     * @param entity
     *            the <code>Entity</code> to be removed from this group, if it is part of it.
     */
    public void remove(Entity<?> entity) {
        if (entity.parent == this) {
            entity.parent = null;
            entities.remove(entity);
            set("children", asString(entities), null);
        }
    }

    /**
     * Adds the given <code>Entity</code> instances to this <code>Group</code>.
     * <p>
     * The entities will be displayed within a container controlled by this <code>Group</code>.
     * 
     * @param entities
     *            the <code>Entity</code> instances to be added to this group.
     * @exception IllegalArgumentException
     *                if at least one given <code>Entity</code> is already in a <code>Group</code>.
     */
    public void add(Entity<?>... entities) {
        Stream.of(entities).forEach(entity -> {
            if (entity.parent != null) {
                throw new IllegalArgumentException();
            }
            this.entities.add(entity);
        });

        set("children", asString(this.entities), null);
    }

    private String asString(Set<Entity<?>> entities) {
        return entities.stream()
                .map(e -> String.valueOf(e.getId()))
                .collect(Collectors.joining(","));
    }

    @Override
    Entity.Type getType() {
        return Entity.Type.GROUP;
    }

}