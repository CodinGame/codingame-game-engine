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
	<version>3.4.7</version>
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
  ToggleModule.defineToggle({
    // The name of the toggle
    // replace "myToggle" by the name of the toggle you want to use
    toggle: 'myToggle',
    // The text displayed over the toggle
    title: 'MY TOGGLE',
    // The labels for the on/off states of your toggle
    values: {
      'TOGGLED ON': true,
      'TOGGLED OFF': false
    },
    // Default value of your toggle
    default: true
  }),
  ToggleModule.defineToggle({
    toggle: 'myOtherToggle',
    ⋮
  })
]
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

