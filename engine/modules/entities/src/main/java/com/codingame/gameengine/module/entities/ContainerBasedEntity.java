package com.codingame.gameengine.module.entities;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 
 *
 * @param <T> a subclass inheriting Entity, used in order to return <b>this</b> as a T instead of a <code>ContainerBasedEntity</code>.
 */
public abstract class ContainerBasedEntity<T extends Entity<?>> extends Entity<T> {

    private Set<Entity<?>> entities;
    
    ContainerBasedEntity() {
        super();

        entities = new HashSet<>();
    }
    /**
     * Separates the given entity from this <code>ContainerBasedEntity</code>.
     * 
     * @param entity
     *            the <code>Entity</code> to be removed from this ContainerBasedEntity, if it is part of it.
     */
    public void remove(Entity<?> entity) {
        if (entity.parent == this) {
            entity.parent = null;
            entities.remove(entity);
            set("children", asString(entities), null);
        }
    }

    /**
     * Adds the given <code>Entity</code> instances to this <code>ContainerBasedEntity</code>.
     * <p>
     * The entities will be displayed within a container controlled by this <code>ContainerBasedEntity</code>.
     * 
     * @param entities
     *            the <code>Entity</code> instances to be added to this ContainerBasedEntity.
     * @exception IllegalArgumentException
     *                if at least one given <code>Entity</code> is already in a <code>ContainerBasedEntity</code>.
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

}
