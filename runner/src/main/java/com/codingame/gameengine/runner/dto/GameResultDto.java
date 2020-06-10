package com.codingame.gameengine.runner.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A data transfer object for game execution data.
 * <p>
 * Used internally.
 * </p>
 */
@SuppressWarnings("javadoc")
public class GameResultDto {
    public Map<String, List<String>> errors = new HashMap<>();
    public Map<String, List<String>> outputs = new HashMap<>();
    public List<String> summaries = new ArrayList<>();
    public List<String> views = new ArrayList<>();
    public Map<Integer, Integer> scores = new HashMap<>();
    public List<String> uinput = new ArrayList<>();
    public String metadata;
    public List<TooltipDto> tooltips = new ArrayList<>();
    public Map<Integer, Integer> ids = new HashMap<>();
    public List<AgentDto> agents = new ArrayList<>();
    public String failCause = null;
}
