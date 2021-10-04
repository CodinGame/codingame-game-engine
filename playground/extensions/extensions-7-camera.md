# CameraModule

This module allows you to have a camera following some of your entities during the replay. 
<br> It comes with the CameraToggleModule which allows the user to enable or disable the camera.

## Showcase
Here is a usage example
<a href="https://imgur.com/BAe8M9d"><img src="https://i.imgur.com/BAe8M9d.gif" title="source: imgur.com" /></a>
## Setup
⚠ This module requires the [GraphicEntityModule](https://github.com/CodinGame/codingame-game-engine/tree/master/engine/modules/entities) to work.

Add the dependency in the `pom.xml` of your project.
```xml
<dependency>
	<groupId>com.codingame.gameengine</groupId>
	<artifactId>module-camera</artifactId>
	<version>3.15.0</version>
</dependency>
```
And add another one if you want the camera toggle 
```xml
<dependency>
    <groupId>com.codingame.gameengine</groupId>
    <artifactId>module-toggle-camera</artifactId>
    <version>3.15.0</version>
</dependency>
```

Then setup the module (and the toggle) in your `config.js`.

```javascript
import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js'
import { CameraModule } from './camera-module/CameraModule.js'

import { CameraToggleModule } from './cameratoggle-module/CameraToggleModule' 
// Only if you want the toggle

// List of viewer modules that you want to use in your game
export const modules = [
  GraphicEntityModule,
  CameraModule, 
  CameraToggleModule // Only if you want the toggle
]

// Insert this only if you want the camera toggle
// You can change the labels it won't affect the program 
export const options = [
    CameraToggleModule.defineToggle({
        // The name of the camera toggle
        toggle: 'cameraMode',
        // The text displayed over the camera toggle
        title: 'CAMERA MODE',
        // The labels for the on/off states of the camera toggle
        values: {
            'DYNAMIC': true, // camera on
            'FIXED': false   // camera off
        },
        // Default value of the camera toggle
        default: true
    })
]

```

## Usage

`Referee.java`
```java
@Inject
CameraModule cameraModule;

@Override
public void init() {
    cameraModule.setContainer(myEntityContainer, 1920, 1080);
    // `myEntityContainer` has to be a predecessor of every tracked entities
    cameraModule.addTrackedEntity(myEntity);
    // `myEntity` has to be a successor of `myEntityContainer` 
}
```
