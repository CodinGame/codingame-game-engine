package com.codingame.gameengine.module.entities;

/**
 * A Group is an Entity which acts as a container for other entities.
 */
public class Group extends ContainerBasedEntity<Group> {

    Group() {
        super();
    }

    @Override
    Entity.Type getType() {
        return Entity.Type.GROUP;
    }

}