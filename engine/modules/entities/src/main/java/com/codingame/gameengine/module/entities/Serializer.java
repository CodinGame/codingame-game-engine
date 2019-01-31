package com.codingame.gameengine.module.entities;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codingame.gameengine.module.entities.Entity.Type;
import com.google.inject.Singleton;

@Singleton
class Serializer {
    public Map<String, String> commands, keys, separators;
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
        keys.put("fontWeight", "fw");
        keys.put("text", "T");
        keys.put("children", "ch");
        keys.put("scaleX", "sx");
        keys.put("scaleY", "sy");
        keys.put("anchorX", "ax");
        keys.put("anchorY", "ay");
        keys.put("visible", "v");
        keys.put("zIndex", "z");
        keys.put("blendMode", "b");
        keys.put("images", "I");
        keys.put("restarted", "rs");
        keys.put("playing", "p");
        keys.put("loop", "l");
        keys.put("duration", "d");
        keys.put("baseWidth", "bw");
        keys.put("baseHeight", "bh");
        keys.put("points", "ps");

        commands = new HashMap<>();
        commands.put("CREATE", "C");
        commands.put("UPDATE", "U");
        commands.put("LOADSPRITESHEET", "L");
        commands.put("WORLDUPDATE", "W");

        separators = new HashMap<>();
        separators.put("COMMAND", ";");
        separators.put("COMMAND_ARGUMENT", " ");
        separators.put("ARGUMENT_DETAILS", ",");
        separators.put("COMMAND_TYPE", "\n");

        curves = new HashMap<>();
        curves.put(Curve.NONE, "_");
        curves.put(Curve.IMMEDIATE, "Γ");
        curves.put(Curve.LINEAR, "/");
        curves.put(Curve.EASE_IN_AND_OUT, "∫");
        curves.put(Curve.ELASTIC, "~");

        types = new HashMap<>();
        types.put(Type.RECTANGLE, "R");
        types.put(Type.ROUNDED_RECTANGLE, "K");
        types.put(Type.CIRCLE, "C");
        types.put(Type.GROUP, "G");
        types.put(Type.BUFFERED_GROUP, "B");
        types.put(Type.LINE, "L");
        types.put(Type.SPRITE, "S");
        types.put(Type.TEXT, "T");
        types.put(Type.BITMAPTEXT, "X");
        types.put(Type.SPRITEANIMATION, "A");
        types.put(Type.POLYGON, "P");

        if (keys.values().stream().distinct().count() != keys.values().size()) {
            throw new RuntimeException("Duplicate keys");
        }
        if (commands.values().stream().distinct().count() != commands.values().size()) {
            throw new RuntimeException("Duplicate commands");
        }
        if (separators.values().stream().distinct().count() != separators.values().size()) {
            throw new RuntimeException("Duplicate separators");
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
        if (separators.values().stream().anyMatch(
            character -> curves.containsValue(character) ||
                keys.containsValue(character) ||
                types.containsValue(character) ||
                commands.containsValue(character)
        )) {
            throw new RuntimeException("String already used as separator");
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

    private String serializeEntitiesStateDiff(Entity<?> entity, EntityState diff, String frameInstant) {
        String meta = join(
            entity.getId(),
            frameInstant
        );
        if (diff.isEmpty()) {
            return meta;
        }
        return join(
            meta,
            minifyDiff(diff)
        );
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
            .collect(Collectors.joining(separators.get("COMMAND_ARGUMENT")));
    }

    private String serializeCreateEntity(Entity<?> e) {
        return join(
            types.get(e.getType())
        );
    }

    public Optional<String> serializeCreateEntities(List<Entity<?>> entities) {
        if (entities.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(
                commands.get("CREATE") + entities.stream()
                    .map(e -> serializeCreateEntity(e))
                    .collect(Collectors.joining(separators.get("COMMAND")))
            );
        }
    }

    private String serializeLoadSpriteSheet(SpriteSheetSplitter spriteSheet) {
        return join(
            spriteSheet.getName(), spriteSheet.getSourceImage(),
            spriteSheet.getWidth(), spriteSheet.getHeight(), spriteSheet.getOrigRow(), spriteSheet.getOrigCol(), spriteSheet.getImageCount(),
            spriteSheet.getImagesPerRow()
        );
    }

    public Optional<String> serializeLoadSpriteSheets(List<SpriteSheetSplitter> spriteSheets) {
        if (spriteSheets.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(
                commands.get("LOADSPRITESHEET") + spriteSheets.stream()
                    .map(e -> serializeLoadSpriteSheet(e))
                    .collect(Collectors.joining(separators.get("COMMAND")))
            );
        }
    }

    public Optional<String> serializeWorldCommits(List<String> worldCommits) {
        if (worldCommits.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(
                commands.get("WORLDUPDATE") +
                worldCommits.stream()
                        .collect(Collectors.joining(separators.get("COMMAND_ARGUMENT")))
            );
        }
    }

    public Optional<String> serializeWorldDiff(List<WorldState> diffs) {
        if (diffs.isEmpty()) {
            return Optional.empty();
        } else {
            List<String> serialized = diffs.stream()
                .map(worldDiff -> {
                    Optional<String> result;
                    Set<Entry<Entity<?>, EntityState>> diff = worldDiff.getEntityStateMap().entrySet();
                    if (diff.isEmpty()) {
                        result = Optional.empty();
                    } else {
                        result = Optional.of(
                            diff
                                .stream()
                                .map(e -> {
                                    return serializeEntitiesStateDiff(e.getKey(), e.getValue(), worldDiff.getFrameTime());
                                })
                                .collect(Collectors.joining(separators.get("COMMAND")))
                        );
                    }
                    return result;
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
            if (serialized.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(
                    commands.get("UPDATE") + serialized
                        .stream()
                        .collect(Collectors.joining(separators.get("COMMAND")))
                );
            }
        }
    }

}