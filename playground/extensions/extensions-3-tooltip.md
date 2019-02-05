# TooltipModule

This module can be used to assign some data to an entity from the [GraphicEntityModule](https://github.com/CodinGame/codingame-game-engine/tree/master/engine/modules/entities). The data will be displayed in a tooltip when the mouse cursor hovers over the entity on screen.

You may change the assigned data of each entity once per game turn.

## Import
⚠ This module requires the [GraphicEntityModule](https://github.com/CodinGame/codingame-game-engine/tree/master/engine/modules/entities) to work.

Add the dependency in the `pom.xml` of your project.
```xml
<dependency>
	<groupId>com.codingame.gameengine</groupId>
	<artifactId>module-tooltip</artifactId>
	<version>3.4.1</version>
</dependency>
```
And load the module in your `config.js`.
```javascript
import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { TooltipModule } from './tooltip-module/TooltipModule.js';

export const modules = [
	GraphicEntityModule,
	TooltipModule
];
```

## Usage

`Referee.java`
```java
@Inject TooltipModule tooltips;

@Override
public void init() {
  // adding a tooltip to an entity
    tooltips.setTooltipText(myEntity, "the tooltip text linked to this entity");

  // removing the tooltip from an other entity
      tooltips.removeTooltipText(otherEntity);

  // getting the tooltip text associated to an entity
    String text = tooltips.getTooltipText(myEntity);
  // in this case text will now be "the tooltip text linked to this entity"
}
```