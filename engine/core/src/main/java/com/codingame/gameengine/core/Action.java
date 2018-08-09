package com.codingame.gameengine.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Action {
    private final static Map<Class<?>, String> ACCEPTED_CLASSES_REGEXES = new HashMap<>();
    private final static Map<Class<?>, Function<String, ?>> ACCEPTED_CLASSES_CALLBACKS = new HashMap<>();
    protected static Log log = LogFactory.getLog(Action.class);

    static {
        ACCEPTED_CLASSES_REGEXES.put(Integer.class, "-?\\d+");
        ACCEPTED_CLASSES_REGEXES.put(Long.class, "-?\\d+");
        ACCEPTED_CLASSES_REGEXES.put(Float.class, "-?\\d+\\.\\d+");
        ACCEPTED_CLASSES_REGEXES.put(Double.class, "-?\\d+\\.\\d+");
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
    private Map<String, Class<?>> parameters = new HashMap<>();
    private boolean message;
    private Pattern pattern;
    private Matcher matcher;

    public Action(String keyword, Map<String, Class<?>> parameters, boolean message) {
        super();
        this.keyword = keyword;
        this.parameters = parameters;
        this.message = message;

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
            if (message && "message".equalsIgnoreCase(parameter)) {
                throw new RuntimeException(
                    "Ambiguous parameter \"message\": you already request an action with a message. Please rename your parameter."
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
        if (message) {
            patternString += "(\\s+(?<message>.+))?";
        }

        return Pattern.compile(patternString + "\\s*$");
    }

    public boolean matches(String command) {
        matcher = pattern.matcher(command);
        if (matcher.matches()) {
            for (String parameter : parameters.keySet()) {
                if (get(parameter) == null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

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

    public String getMessage() {
        checkMatched();

        if (!message) {
            throw new RuntimeException("This action does not handle messages");
        }

        return matcher.group("message");
    }

    private void checkMatched() {
        if (matcher == null) {
            throw new RuntimeException("This action has not been handled by ActionManager nor matched.");
        }
        if (!matcher.matches()) {
            throw new RuntimeException("This action does not match the given String.");
        }
    }

    public String getKeyword() {
        return keyword;
    }
}
