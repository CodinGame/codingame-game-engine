package com.codingame.gameengine.module.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

class EntityState {
    Map<String, Param> map;

    public EntityState(EntityState other) {
        map = new HashMap<>(other.map);
    }

    static class Param {
        Object value;
        Curve curve;

        public Param(Object value, Curve curve) {
            Objects.requireNonNull(curve);

            this.value = value;
            this.curve = curve;
        }

        public boolean equals(Param other) {
            return other != null && other.value.equals(value);
        }
    }

    EntityState() {
        map = new HashMap<>();
    }

    public void forEach(BiConsumer<? super String, ? super Param> action) {
        map.forEach(action);
    }

    public Object put(String key, Param value) {
        return map.put(key, value);
    }

    public Param get(String key) {
        return map.get(key);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<Entry<String, Param>> entrySet() {
        return map.entrySet();
    }

    public void put(String key, Object value, Curve curve) {
        map.put(key, new Param(value, curve));
    }

    EntityState diffFromNonNullOtherState(EntityState prevState) {
        EntityState diff = new EntityState();
        forEach((key, value) -> {
            Param prevValue = prevState.get(key);
            if (!value.equals(prevValue)) {
                diff.put(key, value);
            }
        });
        return diff;
    }

    EntityState diffFromOtherState(Optional<EntityState> optionalPrevState) {
        if (optionalPrevState.isPresent()) {
            return diffFromNonNullOtherState(optionalPrevState.get());
        } else {
            return new EntityState(this);
        }
    }
}