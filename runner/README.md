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
  <version>1.0</version>
</dependency>
```

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

### Using external python programs as players:
```java
GameRunner gameRunner = new GameRunner();

gameRunner.addCommandLinePlayer("python3 /home/user/player1.py");
gameRunner.addCommandLinePlayer("python3 /home/user/player2.py");
gameRunner.addCommandLinePlayer("python3 /home/user/player3.py");

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
## Viewing a replay

Once a game is run, files are copied into a temporary folder. A server is started to serve those files on `localhost:8888`.

The test page `/test.html` let's you watch the replay as it would appear on CodinGame.

Many of the viewers game-specific parameters may be changed by the default `config.js` file located in `src/main/resources/view` of your game's project. These parameters include: 
* The list of modules needed by the game.
* The assets to load at startup.
* The default display before the first game is launched.

### Loading assets
Assets are expected to be placed in the `src/main/resources/view/assets` folder of your game's project.
```javascript
export const assets = {
  baseUrl: 'assets/',
  images: {
    //Each image will be added to the texture cache with the given key.
    background: 'MyBackround.jpg'
  },
  spines: {
    //Not yet implemented
  },
  sprites: [
    //Each frame of the sprite will be added to the texture cache with the key from the json.
    //see https://www.leshylabs.com/apps/sstool/
    'spriteSheet.json'
  ],
  fonts: [
    'myBitmapFont.fnt',
  ],
  others: [
    //Will launch a request for the resource, but cannot yet be used.
  ]
};
```
You can then use the images in the texture cache with the [Entity Module](../modules/entities/):
```java
entityManager.createSprite.setImage("background");
```

TODO: mention that we use PIXI.

# Documentation

- Reference API

- Architecture

- Extending the engine
