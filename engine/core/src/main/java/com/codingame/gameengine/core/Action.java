package com.codingame.gameengine.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An <code>Action</code> parses players outputs. 
 * An action must be either <b>matched</b> (if it is the only possible action) or <b>handled</b> by an <code>ActionManager</b>.
 * <p>
 * It features:
 * <ul>
 *  <li>The keyword of the action</li>
 *  <li>(Optional) A set of parameters</li>
 *  <li>(Optional) Allowing players to send messages</li>
 * </ul>
 */
public class Action {
    private final static Map<Class<?>, String> ACCEPTED_CLASSES_REGEXES = new HashMap<>();
    private final static Map<Class<?>, Function<String, ?>> ACCEPTED_CLASSES_CALLBACKS = new HashMap<>();
    protected static Log log = LogFactory.getLog(Action.class);

    static {
        ACCEPTED_CLASSES_REGEXES.put(Integer.class, "-?\\d+");
        ACCEPTED_CLASSES_REGEXES.put(Long.class, "-?\\d+");
        ACCEPTED_CLASSES_REGEXES.put(Float.class, "-?\\d*\\.\\d+");
        ACCEPTED_CLASSES_REGEXES.put(Double.class, "-?\\d*\\.\\d+");
        ACCEPTED_CLASSES_REGEXES.put(String.class, ".+");
        ACCEPTED_CLASSES_REGEXES.put(Character.class, ".");

        ACCEPTED_CLASSES_CALLBACKS.put(Integer.class, s -> {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return null;
            }
        });
        ACCEPTED_CLASSES_CALLBACKS.put(Long.class, s -> {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return null;
            }
        });
        ACCEPTED_CLASSES_CALLBACKS.put(Float.class, s -> {
            try {
                return Float.parseFloat(s);
            } catch (NumberFormatException e) {
                return null;
            }
        });
        ACCEPTED_CLASSES_CALLBACKS.put(Double.class, s -> {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return null;
            }
        });
        ACCEPTED_CLASSES_CALLBACKS.put(String.class, s -> s);
        ACCEPTED_CLASSES_CALLBACKS.put(Character.class, s -> s.charAt(0));
    }

    private String keyword;
    private Map<String, Class<?>> parameters;
    private boolean allowMessage;
    private Pattern pattern;
    private Matcher matcher;

    public Action(String keyword, Map<String, Class<?>> parameters, boolean message) {
        super();
        this.keyword = keyword;
        this.parameters = parameters;
        this.allowMessage = message;

        checkParameters();
        pattern = createPattern();
    }

    public Action(String keyword, Map<String, Class<?>> parameters) {
        this(keyword, parameters, false);
    }

    public Action(String keyword) {
        this(keyword, false);
    }

    public Action(String keyword, boolean message) {
        this(keyword, new HashMap<>(), message);
    }

    private void checkParameters() {
        for (String parameter : parameters.keySet()) {
            if (allowMessage && "message".equalsIgnoreCase(parameter)) {
                throw new RuntimeException(
                    "Ambiguous parameter \"message\": you already allow an action with a message. Please rename your parameter."
                );
            }
            
            Class<?> parameterClass = parameters.get(parameter);

            if (ACCEPTED_CLASSES_REGEXES.keySet().stream().noneMatch(cls -> parameterClass.equals(cls))) {
                throw new RuntimeException(
                    "Parameter \"" + parameter + "\" of action \"" + keyword + "\" does not have a valid type. It must be one among "
                        + ACCEPTED_CLASSES_REGEXES.keySet().stream().map(cls -> cls.getName()).collect(Collectors.joining(", "))
                );
            }
        }
    }

    private Pattern createPattern() {
        String patternString = "^\\s*" + keyword;
        for (String parameterName : parameters.keySet()) {
            Class<?> parameterClass = parameters.get(parameterName);

            patternString += " (?<" + parameterName + ">" + ACCEPTED_CLASSES_REGEXES.get(parameterClass) + ")";
        }
        if (allowMessage) {
            patternString += "(\\s+(?<message>.+))?";
        }

        return Pattern.compile(patternString + "\\s*$");
    }

    private void checkMatched() {
        if (matcher == null) {
            throw new RuntimeException("This action has not been handled by ActionManager nor matched.");
        }
        if (!matcher.matches()) {
            throw new RuntimeException("This action does not match the given String.");
        }
    }

    /**
     * Checks if the action matches the given instruction and has legal parameters.
     * 
     * @param instruction the <code>String</code> to match
     * @return <b>true</b> if the action is correctly matched.
     */
    public boolean matches(String instruction) {
        matcher = pattern.matcher(instruction);
        return matcher.matches() && parameters.keySet().stream().map(p -> get(p)).noneMatch(Objects::isNull);
    }

    /**
     * Get the value of the given parameter.
     * 
     * @param parameter the name of the parameter
     * @return the value of the parameter as <code>Object</code> that can be safely casted. 
     */
    public Object get(String parameter) {
        if ("message".equalsIgnoreCase(parameter)) {
            return getMessage();
        }
        checkMatched();

        Class<?> parameterClass = parameters.get(parameter);

        if (parameterClass == null) {
            throw new RuntimeException("This action does not contain the parameter \"" + parameter + "\".");
        }

        return ACCEPTED_CLASSES_CALLBACKS.get(parameterClass).apply(matcher.group(parameter));
    }

    /**
     * Get the message the player added to their instruction. As the message is not mandatory, this value can be <code>null</code>.
     * @return the message in the instruction. <code>null</code> if it does not exist.
     */
    public String getMessage() {
        checkMatched();

        if (!allowMessage) {
            throw new RuntimeException("This action does not handle messages");
        }

        return matcher.group("message");
    }

    /**
     * @return the action keyword.
     */
    public String getKeyword() {
        return keyword;
    }
}
