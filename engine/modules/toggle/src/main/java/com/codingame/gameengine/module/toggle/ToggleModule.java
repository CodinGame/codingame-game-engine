package com.codingame.gameengine.module.toggle;

import java.util.HashMap;
import java.util.Map;

import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.Module;
import com.codingame.gameengine.module.entities.Entity;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Jean Porée
 *
 *         This module allows you to display or hide elements of the GraphicEntityModule using the viewer's options menu.
 *
 */
@Singleton
public class ToggleModule implements Module {

    GameManager<AbstractPlayer> gameManager;
    @Inject GraphicEntityModule entityModule;
    Map<Integer, Toggle> registered, newRegistration;

    class Toggle {
        public String name;
        public boolean state = true;

        public Toggle(String name, boolean state) {
            this.name = name;
            this.state = state;
        }

        public boolean equals(Toggle other) {
            return other != null && this.state == other.state && stringEquals(this.name, other.name);
        }

        private boolean stringEquals(String a, String b) {
            if (a == b) {
                return true;
            } else if (a != null && a.equals(b)) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Inject
    ToggleModule(GameManager<AbstractPlayer> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
        registered = new HashMap<>();
        newRegistration = new HashMap<>();
    }

    @Override
    public void onGameInit() {
        sendFrameData();
    }

    @Override
    public void onAfterGameTurn() {
        sendFrameData();
    }

    @Override
    public void onAfterOnEnd() {}

    private void sendFrameData() {
        HashMap<String, String> data = new HashMap<>();
        for (int d : newRegistration.keySet()) {
            String key = newRegistration.get(d).name;
            if (!data.containsKey(key)) data.put(key, "");
            data.put(key, data.get(key) + d + (newRegistration.get(d).state ? "+" : "-"));
        }
        gameManager.setViewData("toggles", data);

        newRegistration.clear();
    }
    /**
     * Will display the entity only when the toggle state matches the state you set
     *
     * @param entity which will be displayed
     * @param toggle the name of the toggle you want to use
     * @param state the state of the toggle where the entity will be displayed at
     */
    public void displayOnToggleState(Entity<?> entity, String toggle, boolean state) {
        int id = entity.getId();
        Toggle associatedToggle = new Toggle(toggle, state);
        if (!associatedToggle.equals(registered.get(id))) {
            newRegistration.put(id, associatedToggle);
            registered.put(id, associatedToggle);
        }
    }
}
