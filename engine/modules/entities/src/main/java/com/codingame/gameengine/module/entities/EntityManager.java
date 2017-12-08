package com.codingame.gameengine.module.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.Module;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EntityManager implements Module {

    //JAVA
    //TODO: animations
    //TODO: masks
    //TODO: warnings and exceptions
    //TODO: javadoc
    //TODO: decide between builder pattern or whatever
    //TODO: dumping should be externalized (maybe?) 
    //TODO: Asynchronous animation system
    //TODO: Allow user to select lerping function somehow (noLerp, bellLerp, easeLerp, etc)

    //JS
    //TODO: Should PIXI be in window or in Drawer ?
    //TODO: sort out the "getGameName()" problem
    //TODO: sort out the "canSwapPlayers()" pbm
    //TODO: make lineWidth and strokeThickness retain size on canvas resize (so that the value in pixels is kept)

    // HTML + main.js
    //TODO: make html look more like IDE / simplify interface
    //TODO: use Drawer implementation found on codingame
    //TODO: add utility functions ? (to simulate IDE)
    //TODO: generic end scene + startscreen
    //TODO: default avatars shouldn't be random hardcoded things

    static int ENTITY_COUNT = 0;

    private List<Entity<?>> newEntities;
    private List<Entity<?>> entities;
    private Map<Double, WorldState> worldStates;
    private World world;
    private boolean lockWorld;
    private WorldState currentWorldState;

    
    
    private GameManager<AbstractPlayer> gameManager;    
    @Inject private  Serializer serializer; 

    @Inject
    public EntityManager(GameManager<AbstractPlayer> gameManager) {
        this.gameManager = gameManager;
        world = new World();
        entities = new ArrayList<>();
        newEntities = new ArrayList<>();
        lockWorld = false;
        worldStates = new HashMap<>();
        currentWorldState = new WorldState(0);

        
        gameManager.registerModule(this);
    }

    /**
     * Creates a new world data object to be used to compute all frames throughout the game.
     * <p>
     * This method may only be called once. This method may not be called after the frames have started being computed.
     * <p>
     * The world's width and height determine how the positions of all entities are mapped onto the viewer.
     * 
     * @param width
     *            The number of units across the width of the visible part of the game. Default is 1920.
     * @param height
     *            The number of units across the height of the visible part of the game. Default is 1080.
     * @return the world data object.
     * @exception IllegalStateException
     *                if the method is called more than once or after the game begins.
     */
    public World createWorld(int width, int height) {
        if (lockWorld) {
            throw new IllegalStateException("Cannot create another world.");
        }
        lockWorld = true;
        world = new World(width, height);
        return world;
    }

    /**
     * @return the world data currently being used for computing frames
     */
    public World getWorld() {
        return world;
    };

    /**
     * Every entity's graphical counterpart, at instant t of the frame being computed, will have the same properties as the java object as they are
     * now.
     * <p>
     * Only the most recent commits are kept for a given t.
     * 
     * @param t
     *            The instant of the frame 0 &ge; t &ge; 1.
     * @exception IllegalArgumentException
     *                if the t is not a valid instant.
     * 
     */
    public void commitWorldState(double t) {
        entities.stream().forEach(entity -> commitEntityState(entity, t));
    }

    /**
     * This entity's graphical counterpart, at instant t of the frame being computed, will have the same properties as the java object as they are
     * now.
     * <p>
     * Only the most recent commit is kept for a given t.
     * 
     * 
     * @param entity
     *            The java object representing a graphical entity.
     * @param t
     *            The instant of the frame 0 &ge; t &ge; 1.
     * @exception IllegalArgumentException
     *                if the t is not a valid instant.
     * @exception NullPointerException
     *                if entity is null.
     * 
     */
    public void commitEntityState(Entity<?> entity, double t) {
        requireValidFrameInstant(t);
        Objects.requireNonNull(entity);

        WorldState state = worldStates.get(t);
        if (state == null) {
            state = new WorldState(t);
            worldStates.put(t, state);
        } else {
            //TODO: log warning.
        }
        state.flushEntityState(entity);
    }

    private static void requireValidFrameInstant(double t) {
        if (t < 0 || t > 1) {
            throw new IllegalArgumentException("Not a valid frame instant: " + t);
        }
    }

    private void sendFrameData() {
        StringBuilder sb = new StringBuilder();

        autocommit();

        newEntities.stream().forEach(e -> {
            dumpNewEntity(e, sb);
        });
        newEntities.clear();

        List<WorldState> orderedStates = worldStates.entrySet().stream()
                .sorted((e1, e2) -> e1.getValue().getFrameTime().compareTo(e2.getValue().getFrameTime()))
                .map(Entry::getValue)
                .collect(Collectors.toList());

        for (WorldState next : orderedStates) {
            dumpWorldStateDiff(currentWorldState, next, sb);
            currentWorldState.updateAllEntities(next);
        }

        worldStates.clear();

        gameManager.setViewData("entitymanager", sb.toString());
    }

    private void dumpNewEntity(Entity<?> e, StringBuilder out) {
        out.append(serializer.serializeCreate(e));
    }

    private void dumpWorldStateDiff(WorldState previous, WorldState next, StringBuilder out) {
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
                        out.append(serializer.serializeUpdate(entity, diff, next.getFrameTime()));
                        out.append("\n");
                    }
                });
    }   

    private void autocommit() {
        WorldState state = worldStates.computeIfAbsent(1d, (key) -> new WorldState(1));
        state.flushMissingEntities(entities);
    }

    /**
     * Creates a new Circle entity, its graphical counterpart will be created on the frame currently being computed.
     * 
     * @return the entity. Modify its properties to animate the graphical counterpart.
     */
    public Circle createCircle() {
        Circle c = new Circle();
        newEntity(c);
        return c;
    }

    /**
     * Creates a new Sprite entity, its graphical counterpart will be created on the frame currently being computed.
     * 
     * @return the entity. Modify its properties to animate the graphical counterpart.
     */
    public Sprite createSprite() {
        Sprite c = new Sprite();
        newEntity(c);
        return c;
    }

    /**
     * Creates a new Line entity, its graphical counterpart will be created on the frame currently being computed.
     * 
     * @return the entity. Modify its properties to animate the graphical counterpart.
     */
    public Line createLine() {
        Line c = new Line();
        newEntity(c);
        return c;
    }

    /**
     * Creates a new Rectangle entity, its graphical counterpart will be created on the frame currently being computed.
     * 
     * @return the entity. Modify its properties to animate the graphical counterpart.
     */
    public Rectangle createRectangle() {
        Rectangle c = new Rectangle();
        newEntity(c);
        return c;
    }

    /**
     * Creates a new Text entity, its graphical counterpart will be created on the frame currently being computed.
     * 
     * @param string
     *            The default string for the text. Can be changed.
     * @return the entity. Modify its properties to animate the graphical counterpart.
     */
    public Text createText(String string) {
        Text e = new Text();
        e.setText(string);
        newEntity(e);
        return e;

    }

    /**
     * Creates a new Group entity, its graphical counterpart will be created on the frame currently being computed.
     * <p>
     * A Group represents a collection of other entities. It acts as a container.
     * 
     * @return the entity. Modify its properties to animate the graphical counterpart.
     */
    public Group createGroup(Entity<?>... entities) {
        Group e = new Group();
        newEntity(e);
        e.add(entities);
        return e;

    }

    private void newEntity(Entity<?> e) {
        lockWorld = true;
        entities.add(e);
        newEntities.add(e);
    }

    private void sendGlobalData() {
        gameManager.setViewGlobalData("entitymanager", world);
        lockWorld = true;
    }

    @Override
    public final void onGameInit() {
        sendGlobalData();
        sendFrameData();
    }

    @Override
    public final void onAfterGameTurn() {
        sendFrameData();
    }

    @Override
    public final void onAfterOnEnd() {
        sendFrameData();
    }

}