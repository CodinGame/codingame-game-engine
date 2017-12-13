package com.codingame.gameengine.module.entities;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class Serializer {

    private DecimalFormat decimalFormat;
    @Inject private Gson gson;

    Serializer() {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');

        decimalFormat = new DecimalFormat("0.######");
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setDecimalFormatSymbols(otherSymbols);
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

    static String escape(String text) {
        String escaped = text.replaceAll("\\'", "\\\\'");
        if (escaped.contains(" ")) {
            return "'" + escaped + "'";
        }
        return escaped;
    }

    
    public String serializeUpdate(Entity<?> entity, EntityState diff, Double frameInstant) {
        return join("UPDATE",
                entity.getId(),
                formatFrameTime(frameInstant),
                diff.entrySet().stream()
                        .map((entry) -> {
                            if (entry.getValue() instanceof String) {
                                return join(entry.getKey(), escape((String) entry.getValue()));
                            }
                            return join(entry.getKey(), entry.getValue());
                        })
                        .collect(Collectors.joining(" ")));
    }

    @Deprecated
    public String serialize(Entity<?> entity, EntityState diff, Double frameInstant) {
        gson.toJsonTree(diff.map);
        return join("UPDATE",
                entity.getId(),
                formatFrameTime(frameInstant),
                gson.toJsonTree(diff.map));
    }

    public String serializeCreate(Entity<?> e) {
        return String.format("CREATE %d %s\n", e.getId(), e.getType());
    }

}