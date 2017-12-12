# About

TODO

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

By default, you can access the game viewer for testing at [http://localhost:8888/test.html].

Warning ⚠ To use the game viewer locally, your browser must support ES6 JavaScript **modules**. For Chrome, that's version 61 or more. For Firefox, from version 54 this feature is behind the `dom.moduleScripts.enabled` preference. To change preferences in Firefox, visit `about:config`.


# Examples

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

# Getting Started

TODO

# Documentation

- Reference API

- Architecture

- Extending the engine
