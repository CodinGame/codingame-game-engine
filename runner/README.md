# About

The Game Runner lets you run your game locally during developement. It comes with a handy HTML package to watch each game's replay.

You can create your own AI for your game and use the Game Runner to connect it to your game's implementation.

You can also fiddle with your game's initialization input, such as the seed for random values (for **Multiplayer** games) or a test case file (for **Solo** games, the content of such files is detailed below).

# Usage

Include the dependency below in the pom.xml of your project.
```xml
<dependency>
  <groupId>com.codingame.gameengine</groupId>
  <artifactId>runner</artifactId>
  <version>2.2</version>
</dependency>
```
Or a more recent version.

Instantiate a `MultiplayerGameRunner` or a `SoloGameRunner` to launch a game with the `start()` method. This will create a temporary directory and start a server to serve the files of that directory. You need not stop the previous server to launch a new game.

By default, you can access the game viewer for testing at [http://localhost:8888/test.html](http://localhost:8888/test.html). You may change the configuration of the game viewer by editing the `config.js` file.

Warning ⚠ To use the game viewer locally, your browser must support ES6 JavaScript **modules**. For Chrome, that's version 61 or more. For Firefox, from version 54 this feature is behind the `dom.moduleScripts.enabled` preference. To change preferences in Firefox, visit `about:config`.


# Examples

In order to run a game, you must have prepared a `Referee` and a `Player`. The game will surely fail to finish if they are not properly implemented. See [Game Manager](../engine/core/) for details.

## Running a **Multiplayer** game

### Using the same java class for each player:
```java
MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();
gameRunner.addAgent(Player.class);
gameRunner.addAgent(Player.class);
gameRunner.start();
    
```
⚠ _This method will prevent the agent from printing to stdout from any other class than Player. It has been deprecated for this reason._

### Using external python programs as players:
```java
MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();

gameRunner.addAgent("python3 /home/user/player1.py");
gameRunner.addAgent("python3 /home/user/player2.py");
gameRunner.addAgent("python3 /home/user/player3.py");

gameRunner.start();
```

### Using a custom seed:
```java
// I want to debug the strange case of this particuliar seed: 53295539

Properties refereeInput = new Properties();
refereeInput.put("seed", "53295539");

MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();
gameRunner.setGameParameters(refereeInput);
gameRunner.addAgent(Player1.class);
gameRunner.addAgent(Player2.class);
gameRunner.start();
```

## Running a **Solo** game

### Using a java class and a test case with its filename:
```java
SoloGameRunner gameRunner = new SoloGameRunner();
gameRunner.setTestCase("test1.json"); // You must set a test case to run your game.
gameRunner.setAgent(Player.class);
gameRunner.start();
```

---
### Test case file

You will need to create test case files to run your **Solo** game. If you are creating a **Multiplayer** game, you can skip this section.

Your test cases must be named `test<number>.json` and placed in the `config` directory. Their `<number>` determine the order they will be listed in the CodinGame IDE. Here is an example:

`test1.json`
```json
{
	"title": {
		"2": "One path",
		"1": "Un seul chemin"
	},
	"testIn": ".o...\\n.ooo.\\n...o.",
	"isTest": "true",
	"isValidator": "false"
}
```
- **title:**
    - **2:** English title, this parameter is mandatory.
    - **1:** French title, optional.
- **testIn:** The content of your test case. It can contain multiple lines separated with `\\n`.
- **isTest:** If true, this test will be visible and used as a regular test case.
- **isValidator:** If true, this test will be use to validate the player's code. You can use this to avoid hardcoded solutions.

### Activating game logs

You can view the data that the Referee and the Players send each other by editing the built-in logger's settings.
To do this, open `log4j2.properties` in the root of your project and replace ```rootLogger.level = warn``` with ```rootLogger.level = info```.
Additionally, if you would like to see the output of the different modules, you can use:
`rootLogger.level = trace`.


## Viewing a replay

Once a game is run, files are copied into a temporary folder. A server is started to serve those files on `localhost:8888`.

The test page `/test.html` let's you watch the replay as it would appear on CodinGame.

Many of the viewers game-specific parameters may be changed by the default `config.js` file located in `src/main/resources/view` of your game's project. These parameters include: 
* The list of modules needed by the game.
* The colours for the different players (affects the IDE).

### Loading assets
Assets are expected to be placed in the `src/main/resources/view/assets` folder of your game's project.

You can then use the images in the texture cache with the [Graphic Entity Module](../modules/entities/):
```java
entityManager.createSprite.setImage("background.png");
```

The game's replay is run using an engine based on [Pixi.js](http://www.pixijs.com/).

# Documentation

## Reference API
	TODO
