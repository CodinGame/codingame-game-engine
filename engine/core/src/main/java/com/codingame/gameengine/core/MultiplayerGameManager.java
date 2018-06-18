package com.codingame.gameengine.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.google.inject.Singleton;

/**
 * The <code>AbstractMultiplayerPlayer</code> takes care of running each turn of a multiplayer game and computing each visual frame of the replay. It
 * provides many utility methods that handle instances of your implementation of AbstractMultiplayerPlayer.
 *
 * @param <T>
 *            Your implementation of AbstractMultiplayerPlayer
 */
@Singleton
public final class MultiplayerGameManager<T extends AbstractMultiplayerPlayer> extends GameManager<T> {

    private Properties gameParameters;
    private long seed;

    @Override
    protected void readGameProperties(InputCommand iCmd, Scanner s) {
        // create game properties
        gameParameters = new Properties();
        if (iCmd.lineCount > 0) {
            for (int i = 0; i < (iCmd.lineCount - 1); i++) {
                try {
                    gameParameters.load(new StringReader(s.nextLine()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        seed = ThreadLocalRandom.current().nextLong();
        if (gameParameters.containsKey("seed")) {
            try {
                seed = Long.parseLong(gameParameters.getProperty("seed"));
            } catch (NumberFormatException e) {
                log.warn("The seed property is not a number, it is reserved by the CodinGame platform to run arena games.");
            }
        }
        gameParameters.setProperty("seed", String.valueOf(seed));
    }

    @Override
    protected void dumpGameProperties() {
        out.println(OutputCommand.UINPUT.format(gameParameters.size()));
        log.info(OutputCommand.UINPUT.format(gameParameters.size()));
        for (Entry<Object, Object> t : gameParameters.entrySet()) {
            out.println(t.getKey() + "=" + t.getValue());
            log.info(t.getKey() + "=" + t.getValue());
            
        }
    }

    /**
     * Get initial number of players.
     * 
     * @return the number of players.
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * <p>
     * The seed is used to generated parameters such as width and height.<br>
     * If a seed is present in the given input, the input value should override the generated values.
     * </p>
     * 
     * @return an <code>long</code> containing a given or generated seed.
     */
    public long getSeed() {
        return seed;
    }

    /**
     * <p>
     * The game parameters are used to get additional information from the Game Runner.
     * </p>
     * 
     * @return a <code>Properties</code> containing the given parameters.
     */
    public Properties getGameParameters() {
        return gameParameters;
    }

    /**
     * Get all the players.
     * 
     * @return the list of players.
     */
    public List<T> getPlayers() {
        return players;
    }

    /**
     * Get all the active players.
     * 
     * @return the list of active players.
     */
    public List<T> getActivePlayers() {
        // TODO: could be optimized with a list of active players updated on player.deactivate().
        return players.stream().filter(p -> p.isActive()).collect(Collectors.toList());
    }

    /**
     * Get player with index i
     * 
     * @param i
     *            Player index
     * @return player with index i
     * @throws IndexOutOfBoundsException
     *             if there is no player at that index
     */
    public T getPlayer(int i) throws IndexOutOfBoundsException {
        return this.players.get(i);
    }

    /**
     * Set game end.
     */
    public void endGame() {
        super.endGame();
    }

    @Override
    protected boolean allPlayersInactive() {
        return getActivePlayers().isEmpty();
    }

    @Override
    protected OutputCommand getGameSummaryOutputCommand() {
        return OutputCommand.SUMMARY;
    }
}
