package com.codingame.gameengine.core;

import java.util.List;

public class IllegalActionException extends Exception {

    private static final long serialVersionUID = 5245116991507916034L;
    
    private String command;
    private List<ActionDescriptor> actions;

    public IllegalActionException(String command, List<ActionDescriptor> actions) {
        this.command = command;
        this.actions = actions;
    }

    public String getMessage() {
        return command; //TODO: include expected actions
    }
}
