package com.codingame.gameengine.module.entities;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codingame.gameengine.module.entities.Entity.Type;
import com.google.inject.Singleton;

@Singleton
class Serializer {

    Map<String, String> commands, keys;
    Map<Entity.Type, String> types;
    Map<Curve, String> curves;
    private static DecimalFormat decimalFormat;
    static {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        decimalFormat = new DecimalFormat("0.######");
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setDecimalFormatSymbols(otherSymbols);
    }

    Serializer() {
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
        keys.put("visible", "v");
        keys.put("zIndex", "z");
        keys.put("blendMode", "b");
        keys.put("images", "I");
        keys.put("started", "p");
        keys.put("loop", "l");
        keys.put("duration", "d");
        keys.put("baseWidth", "bw");
        keys.put("baseHeight", "bh");

        commands = new HashMap<>();
        commands.put("CREATE", "C");
        commands.put("UPDATE", "U");
        commands.put("LOADSPRITESHEET", "L");

        curves = new HashMap<>();
        curves.put(Curve.NONE, "_");
        curves.put(Curve.IMMEDIATE, "Γ");
        curves.put(Curve.LINEAR, "/");
        curves.put(Curve.EASE_IN_AND_OUT, "∫");
        curves.put(Curve.ELASTIC, "~");

        types = new HashMap<>();
        types.put(Type.RECTANGLE, "R");
        types.put(Type.CIRCLE, "C");
        types.put(Type.GROUP, "G");
        types.put(Type.LINE, "L");
        types.put(Type.SPRITE, "S");
        types.put(Type.TEXT, "T");
        types.put(Type.SPRITEANIMATION, "A");

        if (keys.values().stream().distinct().count() != keys.values().size()) {
            throw new RuntimeException("Duplicate keys");
        }
        if (commands.values().stream().distinct().count() != commands.values().size()) {
            throw new RuntimeException("Duplicate commands");
        }
        if (types.values().stream().distinct().count() != types.values().size()) {
            throw new RuntimeException("Duplicate types");
        }
        if (curves.values().stream().distinct().count() != curves.values().size()) {
            throw new RuntimeException("Duplicate curves");
        }
        if (keys.values().stream().anyMatch(character -> curves.containsValue(character))) {
            throw new RuntimeException("Same string used for a curve and a property");
        }

    }

    /**
     * Join multiple object into a space separated string
     */
    static private String join(Object... args) {
        return Stream.of(args).map(String::valueOf).collect(Collectors.joining(" "));
    }

    static String formatFrameTime(double t) {
        return decimalFormat.format(t);
    }

    static String escape(String text) {
        String escaped = text.replaceAll("\\'", "\\\\'");
        if (escaped.contains(" ")) {
            return "'" + escaped + "'";
        }
        return escaped;
    }

    public String serializeUpdate(Entity<?> entity, EntityState diff, String frameInstant) {
        return join(
                commands.get("UPDATE"),
                entity.getId(),
                frameInstant,
                minifyDiff(diff));
    }

    private String minifyParam(String key, EntityState.Param param) {
        String result;

        if (key.equals("rotation")) {
            result = String.valueOf((int) Math.toDegrees((double) param.value));
        } else if (param.value instanceof Double) {
            result = decimalFormat.format(param.value);
        } else if (param.value instanceof Boolean) {
            result = (boolean) param.value ? "1" : "0";
        } else {
            result = escape(param.value.toString());
        }

        // We don't send the default curve, it will be implied.
        if (param.curve.equals(Curve.DEFAULT)) {
            return result;
        }
        return join(result, curves.get(param.curve));
    }

    private String minifyKey(String key) {
        return keys.getOrDefault(key, key);
    }

    private String minifyDiff(EntityState diff) {
        return diff.entrySet().stream()
                .map((entry) -> join(minifyKey(entry.getKey()), minifyParam(entry.getKey(), entry.getValue())))
                .collect(Collectors.joining(" "));
    }

    public String serializeCreate(Entity<?> e) {
        return join(
                commands.get("CREATE"),
                types.get(e.getType()));
    }

    public Object serializeLoadSpriteSheet(SpriteSheetLoader spriteSheet) {
        return join(commands.get("LOADSPRITESHEET"), spriteSheet.getName(), spriteSheet.getSourceImage(),
                spriteSheet.getWidth(), spriteSheet.getHeight(), spriteSheet.getOrigRow(), spriteSheet.getOrigCol(), spriteSheet.getImageCount(), spriteSheet.getImagesPerRow());
    }

}