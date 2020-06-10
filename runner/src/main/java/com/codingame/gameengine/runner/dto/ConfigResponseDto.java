package com.codingame.gameengine.runner.dto;

/**
 * A data transfer object for level configuration data.
 * <p>
 * Used internally.
 * </p>
 */
@SuppressWarnings("javadoc")
public class ConfigResponseDto {
    public int minPlayers;
    public int maxPlayers;
    public String type;
    public String criteria;
    public String sortingOrder;
    public String criteriaEn;
    public String criteriaFr;
}
