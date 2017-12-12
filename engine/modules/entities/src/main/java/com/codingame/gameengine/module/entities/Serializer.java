package com.codingame.gameengine.module.entities;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codingame.gameengine.module.entities.Entity.Type;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class Serializer {

    Map<String, String> keys;
    Map<String, String> commands;
    Map<Entity.Type, String> types;
    private DecimalFormat decimalFormat;
    @Inject private Gson gson;

    Serializer() {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');

        decimalFormat = new DecimalFormat("0.######");
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setDecimalFormatSymbols(otherSymbols);

        keys = new HashMap<>();
        keys.put("rotation", "r");
        keys.put("radius", "R");
        keys.put("x2", "X");
        keys.put("y2", "Y");
        keys.put("width", "w");
        keys.put("height", "h");
        keys.put("tint", "t");
        keys.put("fillColor", "f");
        keys.put("fillAlpha", "F");
        keys.put("lineColor", "c");
        keys.put("lineWidth", "W");
        keys.put("lineAlpha", "A");
        keys.put("alpha", "a");
        keys.put("image", "i");
        keys.put("strokeThickness", "S");
        keys.put("strokeColor", "sc");
        keys.put("fontFamily", "ff");
        keys.put("fontSize", "s");
        keys.put("text", "T");
        keys.put("children", "C");
        keys.put("scaleX", "sx");
        keys.put("scaleY", "sy");
        keys.put("anchorX", "ax");
        keys.put("anchorY", "ay");
        keys.put("visiblz", "v");
        keys.put("zIndex", "z");
        keys.put("blendMode", "b");

        commands = new HashMap<>();
        commands.put("CREATE", "C");
        commands.put("UPDATE", "U");
        
        types = new HashMap<>();
        types.put(Type.RECTANGLE, "R");
        types.put(Type.CIRCLE, "C");
        types.put(Type.GROUP, "G");
        types.put(Type.LINE, "L");
        types.put(Type.SPRITE, "S");
        types.put(Type.TEXT, "T");

        if (keys.values().stream().distinct().count() != keys.values().size()) {
            throw new RuntimeException("Duplicate keys");
        }
        if (commands.values().stream().distinct().count() != commands.values().size()) {
            throw new RuntimeException("Duplicate commands");
        }
        if (types.values().stream().distinct().count() != types.values().size()) {
            throw new RuntimeException("Duplicate commands");
        }

    }

    /**
     * Join multiple object into a space separated string
     */
    static private String join(Object... args) {
        return Stream.of(args).map(String::valueOf).collect(Collectors.joining(" "));
    }

    private String formatFrameTime(double t) {
        return decimalFormat.format(t);
    }

    
    
    public Object[] serializeUpdate(Entity<?> entity, EntityState diff, Double frameInstant) {
        Object[] res = new Object[] {
                commands.get("UPDATE"),
                entity.getId(),
                formatFrameTime(frameInstant),
                minify(diff.map)
        };
        return res;
    }

    private Map<String, Object> minify(Map<String, Object> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap((entry) -> keys.getOrDefault(entry.getKey(), entry.getKey()), Entry::getValue));
    }

    public Object[] serializeCreate(Entity<?> e) {
        return new Object[] {
                commands.get("CREATE"),
                e.getId(),
                types.get(e.getType())
        };
    }

}