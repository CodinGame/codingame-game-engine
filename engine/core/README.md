# About

This project contains the core code of the CodinGame's toolkit which includes the Game Manager and the viewer's replay engine.

# Usage

Include the dependency below in the pom.xml of your project.
```xml
<dependency>
  <groupId>com.codingame.gameengine</groupId>
  <artifactId>core</artifactId>
  <version>3.4.1</version>
</dependency>
```
Or a more recent version.

# Examples

## The Game Manager

Your project should include the class `Player` and the class `Referee`.
Your `Referee` class may then inject (using Guice) a singleton of `SoloGameManager` or `MultiplayerGameManager` (corresponding to the type of game you want to create) parameterized by your `Player` class.
Your `Player` class should extend from `AbstractSoloPlayer` or `AbstractMultiplayerPlayer`.

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

# Documentation

## Game Manager
### Reference API
	TODO

## Drawer
### Reference API

Three javascript exports are available for your Modules that interact with the viewer.

#### constants.js
Contains general purpose constants.

```javascript
// The size of the drawspace on the viewer's canvas.
const WIDTH = 1920;
const HEIGHT = 1080;
```

#### utils.js
Contains general purpose utility functions.

```javascript
/**
 * return word padded to width with char characters
 */
function paddingString(word, width, char)

/**
 * Returns a random integer from [0;a[ if b is null.
 * Returns a random integer from [a;b[ if b is not null.
 */
function randInt(a, b=null)

/**
 * Gets the number from [a;b] at percentage u
 */
function lerp(a, b, u)

/**
 * Gets the percentage position in [a;b] of number v
 */
function unlerpUnclamped(a, b, v)

/**
 * Gets the angle between start & end at percentage amount
 */
function lerpAngle(start, end, amount, maxDelta)

/**
 * Gets the x,y coordinate between 2 points at percentage p
 */
function lerpPosition(from, to, p)

/**
 * Gets a color which is the RGB interpolation between start and end by percentage amount
 */
function lerpColor(start, end, amount)
/**
 * Gets the percentage position in [a;b] of number v, clamped into [0;1]
 */
function unlerp(a, b, v)

/**
 * calls self.push on all elements of arr
 */
function pushAll(self, arr)

/**
 * Returns the scale needed to fit (srcWidth, srcHeight) inside (maxWidth, maxHeight)
 */
function fitAspectRatio(srcWidth, srcHeight, maxWidth, maxHeight, padding)
```
#### transitions.js
Contains interpolation functions that map a [0;1] input to a [0;1] output.

```javascript
/**
 * Traces a bell shaped curve
 */
function bell(x)

/**
 * The output value quickly increseas and wobbles around 1 a little before settling.
 */
function elastic(t)

/**
 * The output value slowly increases and decreases
 */
function ease(t)
```

