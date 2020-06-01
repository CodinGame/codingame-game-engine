# The Game Manager

This document introduces the core code of the CodinGame's toolkit which includes the Game Manager and the viewer's replay engine.

# Usage

Include the dependency below in the pom.xml of your project.
```xml
<dependency>
  <groupId>com.codingame.gameengine</groupId>
  <artifactId>core</artifactId>
  <version>3.4.1</version>
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

## Working with Guice

Using Guice in your own code is totally optional but since the SDK uses injections to handle the instantiation of its different components, it is important to note a few things:
- An injected field will be instantiated by Guice, those fields with `@Inject` will always be `null` if you instantiate the class yourself using the `new` operator.
- Any simple class can be injected into your `Referee` and you can inject the `Referee` or `GraphicEntityModule` into any simple class.
- Writing a lot of Guice code may cause the game to slow down.

### Example

`Referee.java`
```java
public class Referee extends AbstractReferee {
    @Inject MultiplayerGameManager<Player> gameManager;
    @Inject MyGridMaker gridMaker;

    @Override
    public void init() {
      // Map size can be 5,6,7 or 8
      int mapSize = 5 + new Random(gameManager.getSeed()).nextInt(4)
      gridMaker.init(mapSize);
    }
}
```

`MyGridMaker.java`
```java
public class MyGridMaker {
    @Inject GraphicEntityModule entityModule;

    public static final int cell_size = 20;

    @Override
    public void init(int size) {
      // Use the injected GraphicEntityModule to create a grid
      for (int y = 0; y < size; ++y) {
        for (int x = 0; x < size; ++x) {
          entityModule.createRectangle()
          .setX(x * cell_size)
          .setY(y * cell_size)
          .setWidth(cell_size)
          .setHeight(cell_size);
        }
      }
    }
}
```

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

You can set the maximum number of turns before the game ends (even if there are still active players). If you don't set this parameter, the game will end within **200** turns.

```java
gameManager.setMaxTurns(250);
```

>This parameter is an important performance setting. See the [Guidelines](playground/misc/misc-1-guidelines.md) for more details.

### Turn maximum time

You can set the maximum time allowed to a Player to execute their code for a turn. If you don't set this parameter, the players will have **50**ms to execute.

```java
gameManager.setTurnMaxTime(45);
```

>This parameter is an important performance setting. See the [Guidelines](playground/misc/misc-1-guidelines.md) for more details.

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

### Score calculation

Once the game is published, players can submit their code to acquire a **score**. This **score** is the percentage of validators that the player has successfully passed.
Validators are a specific kind of test case. Make sure you [configure them correctly](core-4-configuration.md#test-case-file).


## Optimization Game Features <a name="optimization-game-features"></a>

An Optimization game is a Solo game with a criteria score, such as `Points`, or `Fuel` as well as the normal validator score.

To configure the optimization criteria, you'll need to track it yourself in the game's code and send it as metadata with the `GameRunner`'s `putMetadata()` method. [More information here](core-4-configuration.md#optimization-game-configuration) on the configuration of an optimization game.

Once your game is correctly configured, we advise you set the criteria score at the end of the game as below:
```java
@Override
public void onEnd() {
    // I set my criteria "Fuel" to what's left of the player's fuel
    gameManager.putMetadata("Fuel", remainingFuel);
}
```

### Rank calculation

When a player submits code in an optimization game, they are assigned:
- An ordinary **score**: the percentage of validators successfully passed.
- A total **critera score**: the sum of the **criteria scores** from each validator.

The player's rank is determiend firstly by their **score**, then by their **total criteria score**. This means a very optimized solution that fails a single validator will rank lower than a poorly optimized solution that achieves 100%.

In case of a draw in both score and total criteria score, the ranking is arbitrary.
