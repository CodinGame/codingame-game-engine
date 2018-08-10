package com.codingame.gameengine.core;

import java.util.List;

public class Action {
    private List<Object> parameters;
    private String message;
    private String keyword;

    Action(String keyword, List<Object> parameters, String message) {
        super();
        this.parameters = parameters;
        this.message = message;
        this.keyword = keyword;
    }

    /**
     * Get the value of the parameter at the given index.
     * 
     * @param parameter
     *            the index of the parameter.
     * @return the value of the parameter as <code>Object</code> that can be safely casted.
     */
    public Object get(int index) {
        try {
            return parameters.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("No parameter at index " + index + ".");
        }
    }

    /**
     * Get the message the player added to their instruction.
     * 
     * @return the message in the instruction.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the action keyword.
     */
    public String getKeyword() {
        return keyword;
    }
}
