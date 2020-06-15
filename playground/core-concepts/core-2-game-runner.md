# The Game Runner

The Game Runner lets you run your game locally during developement. It comes with a handy HTML package to watch each game's replay. The parameters you set to the Game Runner will not affect the final contribution.

You can create your own AI for your game and use the Game Runner to connect it to your game's implementation.

You can also fiddle with your game's initialization input, such as the seed for random values (for **Multiplayer** games) or a [test case file](core-4-configuration.md#test-case-file) (for **Solo** games).

# Usage

Include the dependency below in the pom.xml of your project.
```xml
<dependency>
  <groupId>com.codingame.gameengine</groupId>
  <artifactId>runner</artifactId>
  <version>3.4.1</version>
</dependency>
```
Or a more recent version. See the [Release Notes](playground/misc/misc-3-release-notes.md).

As the Game Runner is meant for testing, you must create a Class with a `main` method in `src/test/java`.

Instantiate a `MultiplayerGameRunner` or a `SoloGameRunner` to launch a game with the `start()` method. This will create a temporary directory and start a server to serve the files of that directory. You don't need to stop the previous server to launch a new game.

In addition, you will need to set **Agents** to the Game Runner. They are programs you must code to test your game, just as if they were players' code submissions.

By default, you can access the game viewer for testing at [http://localhost:8888/test.html](http://localhost:8888/test.html). You may change the configuration of the game viewer by editing the `config.js` file. See the [Viewer configuration](core-4-configuration.md#viewer-configuration) for more details.

**Warning** ⚠ To use the game viewer locally, your browser must support ES6 JavaScript **modules**. For Chrome, that's version 61 or above. For Firefox, from version 54 this feature is behind the `dom.moduleScripts.enabled` preference. To change preferences in Firefox, visit `about:config`.

The `MultiplayerGameRunner` also provides a `setLeagueLevel` method which you can use to test a specific league of your game. The league level is a system property, meaning that the value will be shared between all the instances of `MultiplayerGameRunner` you create. When left unspecified, the first level of the game will be run.


# Examples

In order to run a game, you must have prepared a `Referee` and a `Player`. The game will surely fail to finish if they are not properly implemented. See [Game Manager](core-3-game-manager.md) for details.

## Running a **Multiplayer** game

### Using the same java class for each agent:
```java
MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();
gameRunner.addAgent(Agent.class);
gameRunner.addAgent(Agent.class);
gameRunner.start();
    
```
⚠ _This method will prevent the agent from printing to stdout from any other class than Player._

### Using external python programs as agents:
```java
MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();

gameRunner.addAgent("python3 /home/user/agent1.py");
gameRunner.addAgent("python3 /home/user/agent2.py");
gameRunner.addAgent("python3 /home/user/agent3.py");

gameRunner.start();
```

### Using a custom seed:
```java
// I want to debug the strange case of this particuliar seed: 53295539
MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();
gameRunner.setSeed(53295539L);
gameRunner.addAgent(Agent1.class);
gameRunner.addAgent(Agent2.class);
gameRunner.start();
```

## Running a **Solo** game

### Using a java class and a test case with its filename:
```java
SoloGameRunner gameRunner = new SoloGameRunner();
gameRunner.setTestCase("test1.json"); // You must set a test case to run your game.
gameRunner.setAgent(Solution.class);
gameRunner.start();
```

# Viewing a replay

Once a game is run, files are copied into a temporary folder. A server is started to serve those files on `localhost:8888`.

The test page `/test.html` let's you watch the replay as it would appear on CodinGame.

Many of the viewer's game-specific parameters may be changed by the default `config.js` file located in `src/main/resources/view` of your game's project. These parameters include: 
* The list of modules needed by the game.
* The colours for the different players (affects the IDE).

See the [Viewer configuration](core-4-configuration.md#viewer-configuration) for more details.

# Testing <a name="testing"></a>

You can run your game without launching a server. This is useful to batch test your game in various conditions.

Call the GameRunner's `simulate()` function to launch such a game, it will return a `GameResult` object containing information about the game's execution.

```java
// I want to make sure my randomly generated maps don't cause the game to crash
for (int i = 0; i < 100; ++i) {
  MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();
  gameRunner.setSeed((long) (i * 100));
  gameRunner.addAgent(Agent1.class);
  gameRunner.addAgent(Agent2.class);
  GameResult result = gameRunner.simulate();
}
```

An instance of `GameResult` exposes:
  * `outputs` & `errors` the standard and error outputs of all agents and the referee.
  * `summaries` the game summary as outputted by the GameManager.
  * `scores` the scores assigned to each agent.
  * `uinput` the game's input parameters (including the game's seed).
  * `metadata` extra info generated by your referee. Useful for debugging and mandatory for Optimization games.
  * `tooltips` the list of tooltips to be displayed on the replay's progress bar.
  * `agents` a list of objects representing the agents. Contains their index, avatar and nickname.
  * `views` the replay data, useful when creating a Module.



