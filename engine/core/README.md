# About

TODO

# Usage

Include the dependency below in the pom.xml of your project.
```xml
<dependency>
  <groupId>com.codingame.gameengine</groupId>
  <artifactId>core</artifactId>
  <version>1.1</version>
</dependency>
```


# Examples

## The Game Manager

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



# Documentation

- Reference API

- Architecture

- Extending the engine

## Game Manager

## Drawer

- TODO: mention constants.js, utils.js & transitions.js


