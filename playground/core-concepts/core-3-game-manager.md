# The Game Manager

This document introduces the core code of the CodinGame's toolkit which includes the Game Manager and the viewer's replay engine.

# Usage

Include the dependency below in the pom.xml of your project.
```xml
<dependency>
  <groupId>com.codingame.gameengine</groupId>
  <artifactId>core</artifactId>
  <version>2.4</version>
</dependency>
```
Or a more recent version. See the [Release Notes](misc/misc-3-release-notes.md).


## Basic Implementation

Your project should include the class `Player` and the class `Referee`.
Your `Referee` class may then inject (using Guice) a singleton of `SoloGameManager` or `MultiplayerGameManager` (corresponding to the type of game you want to create) parameterized by your `Player` class.
Your `Player` class should extend `AbstractSoloPlayer` or `AbstractMultiplayerPlayer`.

Example for a **Multiplayer** game:
```java
class Player extends AbstractMultiplayerPlayer {
    @Override
    public int getExpectedOutputLines() {
        return 1;
    }
}

public class Referee extends AbstractReferee {
    @Inject private MultiplayerGameManager<Player> gameManager;
    @Override
    public void init() {
    }

    @Override
    public void gameTurn(int turn) {
    }

    @Override
    public void onEnd() {
    }
}
```

Example for a **Solo** game:
```java
class Player extends AbstractSoloPlayer {
    @Override
    public int getExpectedOutputLines() {
        return 1;
    }
}

public class Referee extends AbstractReferee {
    @Inject private SoloGameManager<Player> gameManager;
    @Override
    public void init() {
    }

    @Override
    public void gameTurn(int turn) {
    }

    @Override
    public void onEnd() {
    }
}
```

The Game Manager's API will thus work with your `Player` class, which you may modify at leisure.

# Features

## General Features
This section introduces the different features of the Game Manager for any type of game.

For type-specific features, see:
- [Multiplayer Game Features](#multiplayer-game-features)
- [Solo Game Features](#solo-game-features)
- [Optimization Game Features](#optimization-game-features)
>Note that an Optimization game *is* a Solo game with more settings

### Players

You can get your `Player` instances from the Game Manager with `getPlayer` methods. They allow you to interact with the players' AIs.

You can use the `getNicknameToken()` and `getAvatarToken()` methods to get tokens that will be converted into the real corresponding information by the viewer.

To allow the AIs to play:
- You must send input data to your players with `sendInputLine()`. 
- Execute one turn of their code with `execute()`
- Finally, get their output with `getOutputs()` and use them in your game.

**Timeout**
If a player times out (send an invalid value, takes too long to execute ...) you will be sent a `TimeoutException`. You can use this to end the game or deactivate the player, for example.

### Maximum number of turns

You can set the maximum number of turns before the game ends (even if there are still active players). If you don't set this paramter, the game will end within **400** turns.

```java
gameManager.setMaxTurns(200);
```

>This parameter is an important performance setting. See the [Guidelines](misc/misc-1-guidelines.md) for more details.

### Turn maximum time

You can set the maximum time allowed to a Player to execute their code for a turn. If you don't set this paramter, the players will have **50**ms to execute.

```java
gameManager.setTurnMaxTime(45);
```

>This parameter is an important performance setting. See the [Guidelines](misc/misc-1-guidelines.md) for more details.

### Tooltips

Tooltips will appear on the replay of the current game. They are usually short and describe when a player loses or timeout.

```java
gameManager.addTooltip(player, player.getNicknameToken() + " timeout!");
```

### Game Summary

You can add texts that will be displayed to all players in the game summary, under the viewer.

```java
gameManager.addToGameSummary(String.format("%s pushed %s!", player1.getNicknameToken(), player2.getNicknameToken()));
```

### Frame duration

You can modify the duration of a frame displayed in the viewer.

```java
gameManager.setFrameDuration(2000);
```

The duration is in milliseconds and the default one is 1000ms.

You can also get this value with `gameManager.getFrameDuration();`.

## Multiplayer Game Features <a name="multiplayer-game-features"></a>

In a Multiplayer game, you will need to use the `MultiplayerGameManager` implementation parameterized with your `Player`.

### Game parameters

The Multiplayer Game Manager gives you access to **Game parameters**. These are used to vary your game and allow configuration in the CodinGame IDE Options. The game parameters is a `Properties` object you can get with `getGameParameters()`.

By default, the game parameters contain a randomized `seed`.

### Active Players

All the players are active at the beginning of a battle. If they lose or timeout, you can choose to `deactivate()` them. They will no longer be in the list of players obtained with `getActivePlayers()`.

### End Game

You may want to end the game before the maximum number of turns is reached. You can use `endGame()` whenever one of the players meets the victory condition.

## Solo Game Features <a name="solo-game-features"></a>

In a Solo game, you will need to use the `SoloGameManager` implementation parameterized with your `Player`.

### Test cases

Your game will need at least one test case. See [Test case files](core-4-configuration.md#test-case-file) for more details on their creation.

The Solo Game Manager provides you the test case input with `getTestCase()`.

### End Game

In Solo games, you do not compete against other players. Therefore, you will need to decide when a player *wins* or *loses*. To do that, you have two methods:
```java
gameManager.winGame();
```
and
```java
gameManager.loseGame();
```

## Optimization Game Features <a name="optimization-game-features"></a>

An Optimization game is a Solo game with a score. The only differences comes in the [configuration](core-4-configuration.md#optimization-game-configuration) and the metadata you need to send.

Once your game is correctly configured, you need to send your player's score. We advise you set it at the end of the game as below:
```java
@Override
public void onEnd() {
    // I set my criteria "Fuel" to what's left of the player's fuel
    gameManager.putMetadata("Fuel", remainingFuel);
}
```

### Score calculation

Once the game is online, players will be able to submit their code and a score will be calculated to determine their rank in the leaderboard.

This score corresponds to **the sum of all the scores obtained when running validators**. Validators are specific kinds of test cases. Make sure you [configure them correctly](core-4-configuration.md#test-case-file).
