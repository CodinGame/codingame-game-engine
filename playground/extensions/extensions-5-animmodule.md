# AnimModule
This module is not a maven module, you can find its source code on Github: [https://github.com/CodinGame/codingame-sdk-anim-module](https://github.com/CodinGame/codingame-sdk-anim-module)

This module is useful for games with a lot of AnimatedSprites overlayed over the rest of the game.

Instead of having the Referee output the entire sequence of images for each new animation, you may have it reference a pre-existing animation as defined in `AnimData.js` and configured in `AnimModule.js`.

### Example
`Referee.java`
```java
  // Perform animation labelled "death" starting at t and lasting till end of frame
  double t = 0.2;
  Anim anim = animModule.createAnimationEvent('death', t);
  anim.getParams().put("duration", 1 - t);
```
`AnimModule.js`
```javascript
  anim.started = {frame: number, t: anim.t};
  anim.duration = 
    anim.params.duration // If duration is given, use it
    || 
    DURATIONS[a.id] // Else use predefined duration for this anim id
    || 
    1; // Else animate it for the length of 1 frame
  
```

⚠ This example might require you modify it for proper use in your own game.