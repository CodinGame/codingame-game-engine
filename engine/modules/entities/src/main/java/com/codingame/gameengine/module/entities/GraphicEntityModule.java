package com.codingame.gameengine.module.entities;

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
import com.codingame.gameengine.core.Module;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * The GraphicEntityModule takes care of displaying and animating graphical entities on the replay of the game.
 * <p>
 * </p>
 * Use it by creating shapes, sprites, texts etc, then commiting their states to a certain moment of the frame. By default, the states are commited
 * automatically at the end of the frame.
 */
@Singleton
public class GraphicEntityModule implements Module {

    //JAVA
    //TODO: masks
    //TODO: extra properties for Texts (text wrapping, alignement, ...)

    static int ENTITY_COUNT = 0;

    private List<SpriteSheetLoader> newSpriteSheets;
    private List<Entity<?>> newEntities;
    private List<Entity<?>> entities;
    private Map<String, WorldState> worldStates;
    private World world;
    private boolean lockWorld;
    private WorldState currentWorldState;

    private GameManager<AbstractPlayer> gameManager;
    @Inject private Serializer serializer;
    @Inject private Provider<SpriteSheetLoader> spriteSheetProvider;

    @Inject
    GraphicEntityModule(GameManager<AbstractPlayer> gameManager) {
        this.gameManager = gameManager;
        world = new World();
        entities = new ArrayList<>();
        newEntities = new ArrayList<>();
        newSpriteSheets = new ArrayList<>();
        lockWorld = false;
        worldStates = new HashMap<>();
        currentWorldState = new WorldState("0");

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
            throw new IllegalStateException("World creation must be the first use of this module.");
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
    }

    void loadSpriteSheet(SpriteSheetLoader spritesheet) {
        newSpriteSheets.add(spritesheet);
    }
    
    /**
     * Create a spritesheet loader.
     * @return a SpriteSheetLoader
     */
    public SpriteSheetLoader createSpriteSheetLoader() {
        return spriteSheetProvider.get();
    }

    /**
     * Every entity's graphical counterpart, at instant t of the frame being computed, will have the same properties as the java object as they are
     * now.
     * <p>
     * Only the most recent commits are kept for a given t.
     * </p>
     * <p>
     * If an entity hasn't changed since its previous commit, its commit is ignored.
     * </p>
     * 
     * @param t
     *            The instant of the frame 0 &ge; t &ge; 1.
     * @exception IllegalArgumentException
     *                if the t is not a valid instant.
     * 
     */
    public void commitWorldState(double t) {
        commitEntityState(t, entities.toArray(new Entity[entities.size()]));
    }

    /**
     * This entity's graphical counterpart, at instant t of the frame being computed, will have the same properties as the java object as it is now.
     * <p>
     * Only the most recent commit is kept for a given t.
     * <p>
     * If the entity hasn't changed since its previous commit, the commit is ignored.
     * 
     * @param t
     *            The instant of the frame 0 &ge; t &ge; 1.
     * @param entities
     *            The entity objects to commit.
     * @exception IllegalArgumentException
     *                if the t is not a valid instant or id entities is empty.
     * 
     */
    public void commitEntityState(double t, Entity<?>... entities) {
        requireValidFrameInstant(t);
        requireNonEmpty(entities);

        String actualT = Serializer.formatFrameTime(t);
        
        WorldState state = worldStates.get(actualT);
        if (state == null) {
            state = new WorldState(actualT);
            worldStates.put(actualT, state);
        }

        final WorldState finalState = state;
        Stream.of(entities).forEach(entity -> finalState.flushEntityState(entity));

    }

    private void requireNonEmpty(Object[] items) {
        if (items.length == 0) {
            throw new IllegalArgumentException("Must not be an empty array");
        }
    }

    private static void requireValidFrameInstant(double t) {
        if (t < 0 || t > 1) {
            throw new IllegalArgumentException("Not a valid frame instant: " + t);
        }
    }

    private void sendFrameData() {
        List<Object> commands = new ArrayList<>();

        autocommit();

        newSpriteSheets.forEach(e -> dumpLoadSpriteSheet(e, commands));
        newSpriteSheets.clear();

        newEntities.stream().forEach(e -> {
            dumpNewEntity(e, commands);
        });
        newEntities.clear();

        List<WorldState> orderedStates = worldStates.entrySet().stream()
                .sorted((e1, e2) -> e1.getValue().getFrameTime().compareTo(e2.getValue().getFrameTime()))
                .map(Entry::getValue)
                .collect(Collectors.toList());

        for (WorldState next : orderedStates) {
            dumpWorldStateDiff(currentWorldState, next, commands);
            currentWorldState.updateAllEntities(next);
        }

        worldStates.clear();

        gameManager.setViewData("entitymodule", commands);
    }

    private void dumpNewEntity(Entity<?> e, List<Object> commands) {
        commands.add(serializer.serializeCreate(e));
    }
    
    private void dumpLoadSpriteSheet(SpriteSheetLoader spriteSheet, List<Object> commands) {
        commands.add(serializer.serializeLoadSpriteSheet(spriteSheet));
    }

    private void dumpWorldStateDiff(WorldState previous, WorldState next, List<Object> commands) {
        next.getEntityStateMap()
                .forEach((entity, state) -> {
                    Optional<EntityState> prevState = Optional.ofNullable(previous.getEntityStateMap().get(entity));
                    EntityState diff = new EntityState();
                    state.forEach((key, value) -> {
                        EntityState.Param prevValue = prevState
                                .map(s -> s.get(key))
                                .orElse(null);
                        if (!value.equals(prevValue)) {
                            diff.put(key, value);
                        }
                    });

                    // Forced commits are sent even if they are empty
                    if (!next.isAutocommit() || !diff.isEmpty()) {
                        commands.add(serializer.serializeUpdate(entity, diff, next.getFrameTime()));
                    }
                });
    }

    private void autocommit() {
        WorldState state = worldStates.computeIfAbsent("1", (key) -> new WorldState("1", true));
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
     * @param entities
     *            0 or more entities to immediately add to this group.
     * 
     * @return the entity. Modify its properties to animate the graphical counterpart.
     */
    public Group createGroup(Entity<?>... entities) {
        Group e = new Group();
        newEntity(e);
        e.add(entities);
        return e;

    }

    /**
     * Creates a new Sprite animation, its graphical counterpart will be created on the frame currently being computed.
     * 
     * @return the entity. Modify its properties to animate the graphical counterpart.
     */
    public SpriteAnimation createSpriteAnimation() {
        SpriteAnimation c = new SpriteAnimation();
        newEntity(c);
        return c;
    }

    private void newEntity(Entity<?> e) {
        lockWorld = true;
        entities.add(e);
        newEntities.add(e);
    }

    private void sendGlobalData() {
        gameManager.setViewGlobalData("entitymodule", world);
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
