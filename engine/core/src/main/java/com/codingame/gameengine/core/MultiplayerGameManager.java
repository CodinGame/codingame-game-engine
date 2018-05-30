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

@Singleton
public final class MultiplayerGameManager<T extends AbstractMultiplayerPlayer> extends GameManager<T>{

    private Properties gameProperties;
    private int seed;

    @Override
    protected void readGameProperties(InputCommand iCmd, Scanner s) {
        // create game properties
        gameProperties = new Properties();
        if (iCmd.lineCount > 0) {
            for (int i = 0; i < (iCmd.lineCount - 1); i++) {
                try {
                    gameProperties.load(new StringReader(s.nextLine()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!gameProperties.containsKey("seed")) {
            seed = ThreadLocalRandom.current().nextInt();
            gameProperties.setProperty("seed", String.valueOf(seed));
        } else {
            seed = Integer.parseInt(gameProperties.getProperty("seed"));
        }
    }

    @Override
    protected void dumpGameProperties()  {
        out.println(OutputCommand.UINPUT.format(gameProperties.size()));
        for (Entry<Object, Object> t : gameProperties.entrySet()) {
            out.println(t.getKey() + "=" + t.getValue());
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
     * @return an <code>int</code> containing a given or generated seed
     */
    public int getSeed() {
        return seed;
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
