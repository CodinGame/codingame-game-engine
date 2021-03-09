# Create your own modules

To create a module, you will need a minimum of two files:
- A Java file containing a class implementing `Module`. Its purpose is to send data to the view.
- A Javascript file that will receive the data and interact with the viewer.

You will also need to import your module to the `src/main/resources/view/config.js` file, in the `modules` array.

```javascript
import { GraphicEntityModule } from './MyModule.js';

export const modules = [
	MyModule
];
```

## Back end

### Module class

Create a Java class that implements `Module` and add the implemented methods. Then add a constructor that injects (using Guice) an instance of `GameManager<AbstractPlayer>` and register your module to the game manager using `gameManager.registerModule(this)`. This file must be placed under `src/main/java`.

Once this is done, your class should look like this:
```java
public class MyModule implements Module {
    private GameManager<AbstractPlayer> gameManager;

    @Inject
    MyModule(GameManager<AbstractPlayer> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
    }

    /**
     * Called at the beginning of the game
     */
    @Override
    public final void onGameInit() {
    }

    /**
     * Called at the end of every turn, after the Referee's gameTurn()
     */
    @Override
    public final void onAfterGameTurn() {
    }
    
    /**
     * Called at the end of the game, after the Referee's onEnd()
     */
    @Override
    public final void onAfterOnEnd() {
    }
}
```

### Sending data

The [Game Manager](playground/core-concepts/core-3-game-manager.md) offers two methods for you to send data to your Javascript module. You should use them in the methods implemented from `Module`.

```java
/**
 * Set data for use by the viewer, for the current frame, for a specific module.
 * 
 * @param moduleName
 *            the name of the module
 * @param data
 *            any object that can be serialized in JSON using gson.
 */
public void setViewData(String moduleName, Object data)

/**
 * Set data for use by the viewer and not related to a specific frame. This must be use in the init only.
 * 
 * @param moduleName
 *            the name of the module
 * @param data
 *            any object that can be serialized in JSON using gson.
 */
public void setViewGlobalData(String moduleName, Object data)
```

The `moduleName` corresponds to the same name you will set in your Javascript file (more details below).

## Front end

Create a Javascript file under `src/main/resources/view` that exports your module as a class. A module has several methods called by the SDK that allows it to interact with the viewer.

MyModule.js
```javascript
export class MyModule {
  /**
   * Corresponds to the moduleName variable used in the Java module.
   */
  static get moduleName () {
    return 'myModuleName'
  }

  /**
   * Called when data is received.
   * Handles data for the given frame. Returns data that will be sent as parameter to updateScene.
   * @param frameInfo information about the current frame.
   * @param frameData data that has been sent from the Java module.
   */
  handleFrameData (frameInfo, frameData) {
    // Handle your data here

    // Return what is necessary to your module
    return { frameInfo, frameData }
  }

  /**
   * Called when global data is received. Should only be called on init.
   * @param players information about players, such as avatar, name, color..
   * @param globalData data that has been sent from the Java module.
   */
  handleGlobalData (players, globalData) {
    this.globalData = {
      players: players,
      playerCount: players.length
    }
  }

  /**
   * Called when the scene needs an update.
   * @param previousData data from the previous frame.
   * @param currentData data of the current frame.
   * @param progress progress of the frame. 0 <= progress <= 1
   * @param speed the speed of the viewer, setted up by the user.
   */
  updateScene (previousData, currentData, progress, speed) {

  }

  /**
   * Called when the viewer needs to be rerendered (init phase, resized viewer).
   * @param container a PIXI Container. Add your elements to this object.
   * @param canvasData canvas data containing width and height.
   */
  reinitScene (container, canvasData) {

  }

  /**
   * Called every delta milliseconds.
   * @param delta time between current and last call. Aproximately 16ms by default.
   */
  animateScene (delta) {

  }
}
```

You can now customize the viewer with [PixiJS](http://www.pixijs.com/)! To get started with this lib, we suggest you check the [PIXI.Container documentation](http://pixijs.download/release/docs/PIXI.Container.html) as you will need to add your objects to one.

## Reference API

Three javascript exports are available for your Modules that interact with the viewer.

### constants.js
Contains general purpose constants.

```javascript
// The size of the drawspace on the viewer's canvas.
const WIDTH = 1920;
const HEIGHT = 1080;
```

### utils.js
Contains general purpose utility functions.

```java
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
### transitions.js
Contains interpolation functions that map a [0;1] input to a [0;1] output.

```java
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

