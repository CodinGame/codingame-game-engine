# About

The Game Runner lets you run your game locally during developement. It comes with a handy HTML package to watch each game's replay.

You can create your own AI for your game and use the Game Runner to connect it to your game's implementation.

You can also fiddle with your game's initialization input, such as the seed for random values.

# Usage

Include the dependency below in the pom.xml of your project.
```xml
<dependency>
  <groupId>com.codingame.gameengine</groupId>
  <artifactId>runner</artifactId>
  <version>1.35</version>
</dependency>
```
Or a more recent version.

Instantiate a `GameRunner` to launch a game with the `start()` method. This will create a temporary directory and start a server to serve the files of that directory. You need not stop the previous server to launch a new game.

By default, you can access the game viewer for testing at [http://localhost:8888/test.html](http://localhost:8888/test.html). You may change the configuration of the game viewer by editing the `config.js` file.

Warning ⚠ To use the game viewer locally, your browser must support ES6 JavaScript **modules**. For Chrome, that's version 61 or more. For Firefox, from version 54 this feature is behind the `dom.moduleScripts.enabled` preference. To change preferences in Firefox, visit `about:config`.


# Examples

In order to run a game, you must have prepared a `Referee` and a `Player`. The game will surely fail to finish if they are not properly implemented. See [Game Manager](../engine/core/) for details.

## Running a game

### Using the same java class for each player:
```java
GameRunner gameRunner = new GameRunner();
gameRunner.addJavaPlayer(Player.class);
gameRunner.addJavaPlayer(Player.class);
gameRunner.start();
    
```
⚠ _This method will prevent the agent from printing to stdout from any other class than Player. It has been deprecated for this reason._

### Using external python programs as players:
```java
GameRunner gameRunner = new GameRunner();

gameRunner.addAgent("python3 /home/user/player1.py");
gameRunner.addAgent("python3 /home/user/player2.py");
gameRunner.addAgent("python3 /home/user/player3.py");

gameRunner.start();
```

### Using external python programs as players:
```java
// I want to debug the strange case of this particuliar seed: 53295539

Properties refereeInput = new Properties();
refereeInput.put("seed", "53295539");

GameRunner gameRunner = new GameRunner(refereeInput);
gameRunner.addJavaPlayer(Player1.class);
gameRunner.addJavaPlayer(Player2.class);
gameRunner.start();
```

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
