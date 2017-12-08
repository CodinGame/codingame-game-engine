package com.codingame.gameengine.module.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

class EntityState {
    Map<String, Object> map;

    EntityState() {
        map = new HashMap<>();
    }

    public void forEach(BiConsumer<? super String, ? super Object> action) {
        map.forEach(action);
    }

    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    public Object get(String key) {
        return map.get(key);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }
}