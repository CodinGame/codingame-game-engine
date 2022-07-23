# DisplayOnHoverModule

Contributed by [Butanium](https://github.com/Butanium).

This module allows you to display entities when the mouse is over an entity. It's the
same as the TooltipModule, but with custom entities instead of text.
## Showcase
Here is a usage example <br>
<a href="https://live.staticflickr.com/65535/52235500282_9e6dfdbe65_o.gif"><img src="https://live.staticflickr.com/65535/52235500282_9e6dfdbe65_o.gif"/></a>
## Setup
⚠ This module requires the [GraphicEntityModule](https://github.com/CodinGame/codingame-game-engine/tree/master/engine/modules/entities) to work.

Add the dependency in the `pom.xml` of your project.
```xml
<dependency>
    <groupId>com.codingame.gameengine</groupId>
    <artifactId>module-displayonhover</artifactId>
    <version>4.3.1</version> <!-- todo : update the version once it's released -->
</dependency>
```

Then setup the module in your `config.js`.

```javascript
import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js'
import { DisplayOnHoverModule } from './displayonhover-module/DisplayOnHoverModule.js'

// List of viewer modules that you want to use in your game
export const modules = [
  GraphicEntityModule,
  DisplayOnHoverModule, 
]

// DisplayOnHoverModule.allow_multiple_display = true 
// If the mouse is over multiple entities, it will display the entity linked to all the entities
// Default behavior is to display the entities linked to the first entity the mouse is over
```

## Usage

`Referee.java`
```java
@Inject
CameraModule displayOnHoverModule;

@Override
public void init() {
    // Add enities to display when the mouse is over entity
    displayOnHoverModule.setDisplayHover(entity, entitiesToDisplayWhenTheMouseHoversOverEntity);
    // When you hide entity, untrack it to avoid visual bugs
    displayOnHoverModule.untrack(entity);    
}
```
