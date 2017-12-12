# About

This is the Game Engine Toolkit of CodinGame.

With it one can develop a game for the CodinGame platform. This engine is meant to be imported with maven from a project such as [the Game Engine skeleton](https://github.com/CodinGame/game-skeleton).

# Usage

Include the dependencies below in the pom.xml of your project.
```xml
<dependency>
  <groupId>com.codingame.gameengine</groupId>
  <artifactId>core</artifactId>
  <version>1.1</version>
</dependency>

<dependency>
  <groupId>com.codingame.gameengine</groupId>
  <artifactId>runner</artifactId>
  <version>1.0</version>
</dependency>
```

Instantiate a `GameRunner` to launch a game with the `start()` method. This will create a temporary directory and start a server to serve the files of that directory. You need not stop the previous server to launch a new game.

By default, you can access the game viewer for testing at [http://localhost:8888/test.html].

Warning ⚠ To use the game viewer locally, your browser must support ES6 JavaScript **modules**. For Chrome, that's version 61 or more. For Firefox, from version 54 this feature is behind the `dom.moduleScripts.enabled` preference. To change preferences in Firefox, visit `about:config`.


# Examples

## Using the Game Runner

Using the same java class for each player:
```java
GameRunner gameRunner = new GameRunner();
gameRunner.addJavaPlayer(Player.class);
gameRunner.addJavaPlayer(Player.class);
gameRunner.start();
    
```

Using external python programs as players:
```java
GameRunner gameRunner = new GameRunner();

gameRunner.addCommandLinePlayer("python3 /home/user/player1.py");
gameRunner.addCommandLinePlayer("python3 /home/user/player2.py");
gameRunner.addCommandLinePlayer("python3 /home/user/player3.py");

gameRunner.start();
```

## Using the Game Manager
Your project should include exactly **one** subclass of `AbstractPlayer` and one class which implements `Referee`.
Your `Referee` class may then inject (using Guice) a singleton of `GameManager` parametized by your `AbsractPlayer` subclass.

```java
class MyPlayer extends AbstractPlayer {
    @Override
    public int getExpectedOutputLines() {
        return 1;
    }
}

public class MyReferee implements Referee {
    @Inject private GameManager<MyPlayer> gameManager;
    @Override
    public Properties init(int playerCount, Properties params) {
        return params;
    }

    @Override
    public void gameTurn(int turn) {
    }

    @Override
    public void onEnd() {
    }
}
```
The Game Manager's API will thus work with your `AbstractPlayer` subclass, which you may modify at leisure.

# Getting Started

TODO

# Documentation

- Reference API

- Architecture

- Extending the engine
