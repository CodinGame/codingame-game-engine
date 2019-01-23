# ToggleModule

This module allows you to display or hide elements of the GraphicEntityModule using the viewer's options menu.
This can help to create debug modes.

## Setup
⚠ This module requires the [GraphicEntityModule](https://github.com/CodinGame/codingame-game-engine/tree/master/engine/modules/entities) to work.

Add the dependency in the `pom.xml` of your project.
```xml
<dependency>
	<groupId>com.codingame.gameengine</groupId>
	<artifactId>module-toggle</artifactId>
	<version>3.2.0</version>
</dependency>
```

And setup the module and the toggles in your `config.js`.

```javascript
import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js'
import { ToggleModule } from './toggle-module/ToggleModule.js'

// List of viewer modules that you want to use in your game
export const modules = [
  GraphicEntityModule,
  ToggleModule
]

// The list of toggles displayed in the options of the viewer
export const options = [
  {
  // The name displayed over the toggle
    title: 'MY TOGGLE',
    // Getters and setters for the on/off state of your toggle
    get: function () {
      return toggles.myToggle // replace "myToggle" by the name of the toggle you want to use
    },
    set: function (value) {
      toggles.myToggle = value // replace "myToggle" by the name of the toggle you want to use
      ToggleModule.refreshContent()
    },
    // The labels for the on/off states of your toggle
    values: {
      'TOGGLED ON': true,
      'TOGGLED OFF': false
    }
  },
  {
    title: 'MY OTHER TOGGLE',
    ⋮
  }
]

// The list of the toggles used by the ToggleModule
// replace "myToggle" by the name of the toggle you want to use
export const toggles = {
  myToggle: true, // default value of your toggle
  myOtherToggle: false
}

```

## Usage

`Referee.java`
```java
@Inject ToggleModule toggleModule;

@Override
public void init() {
  // Only display `myEntity` when the state of `myToggle` is `true`
  toggleModule.displayOnToggleState(myEntity, "myToggle", true);
  // My entity will only be displayed when `myToggle` is true 
  // (on the `TOGGLED ON` position according to our previous setup)
}
```

