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
     * Get the value of the given parameter.
     * 
     * @param parameter
     *            the name of the parameter
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
     * Get the message the player added to their instruction. As the message is not mandatory, this value can be <code>null</code>.
     * 
     * @return the message in the instruction. <code>null</code> if it does not exist.
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
