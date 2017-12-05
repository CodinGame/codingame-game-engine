package com.codingame.gameengine.module.entities;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.GameManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EntityManager {

    //TODO: Frame speed
    //TODO: frame t > 1
    //TODO: groups
    //TODO: alpha
    
    static int ENTITY_COUNT = 0;

    @Inject private GameManager<AbstractPlayer> gameManager;
    private List<Entity<?>> newEntities;
    private List<Entity<?>> entities;
    Map<Double, WorldState> worldStates;
    private World world;
    private boolean lockWorld;
    private WorldState previousWorldState;

    private DecimalFormat decimalFormat;

    public EntityManager() {
        world = new World();
        entities = new ArrayList<>();
        newEntities = new ArrayList<>();
        lockWorld = false;
        worldStates = new HashMap<>();
        previousWorldState = new WorldState(0);

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        
        decimalFormat = new DecimalFormat("0.######");
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setDecimalFormatSymbols(otherSymbols);
    }

    public World createWorld(int width, int height) {
        if (lockWorld) {
            throw new RuntimeException("Cannot create another world.");
        }
        lockWorld = true;

        return new World(width, height);
    }

    public World getWorld() {
        return world;
    };

    /**
     * Join multiple object into a space separated string
     */
    static public String join(Object... args) {
        return Stream.of(args).map(String::valueOf).collect(Collectors.joining(" "));
    }

    public void commitWorldState(double t) {
        entities.stream().forEach(entity -> commitEntityState(entity, t));
    }

    //TODO: what to do if entity is created but not updated? 
    public void commitEntityState(Entity<?> entity, double t) {
        WorldState state = worldStates.get(t);
        if (state == null) {
            state = new WorldState(t);
            worldStates.put(t, state);
        } else {
            //TODO: log warning.
        }
        state.flushEntityState(entity);
    }

    //TODO: Bind on send to viewer event
    public void onDumpView() {
        StringBuilder sb = new StringBuilder();
        
        WorldState finalWorldState = autocommit();

        newEntities.stream().forEach(e -> {
            dumpNewEntity(e, sb);
        });
        newEntities.clear();

        List<WorldState> orderedStates = worldStates.entrySet().stream()
                .sorted((e1, e2) -> e1.getValue().getFrameTime().compareTo(e2.getValue().getFrameTime()))
                .map(Entry::getValue)
                .collect(Collectors.toList());
        WorldState current = previousWorldState;
        for (WorldState next : orderedStates) {
            dumpDiff(current, next, sb);
            current = next;
        }

        worldStates.clear();
        previousWorldState = finalWorldState;
        
        gameManager.setViewData("entitymanager", sb.toString());
    }

    private void dumpNewEntity(Entity<?> e, StringBuilder out) {
        out.append(String.format("CREATE %d %s\n", e.getId(), e.getType()));
    }
    
    //TODO: dumping should be externalized
    private void dumpDiff(WorldState previous, WorldState next, StringBuilder out) {
        next.getEntityStateMap()
                .forEach((entity, state) -> {
                    Optional<EntityState> prevState = Optional.ofNullable(previous.getEntityStateMap().get(entity));
                    EntityState diff = new EntityState();
                    state.forEach((param, value) -> {
                        Object prevValue = prevState
                                .map(s -> s.get(param))
                                .orElse(null);
                        if (!value.equals(prevValue)) {
                            diff.put(param, value);
                        }
                    });
                    if (!diff.isEmpty()) {
                        out.append(join(
                                "UPDATE",
                                entity.getId(),
                                formatFrameTime(next.getFrameTime()),
                                diff.entrySet().stream()
                                        .map((entry) -> join(entry.getKey(), entry.getValue()))
                                        .collect(Collectors.joining(" "))));
                        out.append("\n");
                    }
                });
    }

    public String formatFrameTime(double t) {
        return decimalFormat.format(t);
    }

    private WorldState autocommit() {
        WorldState state = worldStates.computeIfAbsent(1d, (key) -> new WorldState(1));
        state.flushMissingEntities(entities);
        return state;
    }

    public Circle createCircle() {
        Circle c = new Circle();
        newEntity(c);
        return c;
    }
    public Sprite createSprite() {
        Sprite c = new Sprite();
        newEntity(c);
        return c;
    }
    public Line createLine() {
        Line c = new Line();
        newEntity(c);
        return c;
    }
    public Rectangle createRectangle() {
        Rectangle c = new Rectangle();
        newEntity(c);
        return c;
    }

    private void newEntity(Entity<?> e) {
        lockWorld = true;
        entities.add(e);
        newEntities.add(e);
    }
}