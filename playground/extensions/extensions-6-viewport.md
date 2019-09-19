# ViewportModule

This module allows you to create a viewport where you can zoom with mousewheel, and click/drag to move.

## Import

⚠ This module requires the [GraphicEntityModule](https://github.com/CodinGame/codingame-game-engine/tree/master/engine/modules/entities) to work.

Add this dependency in the `pom.xml` of your project:

```xml
<dependency>
  <groupId>com.codingame.gameengine</groupId>
  <artifactId>module-viewport</artifactId>
  <version>3.7.0</version>
</dependency>
```

And load the module in your `config.js`:

```javascript
import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { ViewportModule } from './viewport-module/ViewportModule.js';

export const modules = [
  GraphicEntityModule,
  ViewportModule
];
```

## Usage

In your `Referee.java`:

```java
@Inject private GraphicEntityModule gem;
@Inject ViewportModule viewportModule;

@Override
public void init() {
  Group group = gem.createGroup();

  // group.add(your entities)

  viewportModule.createViewport(group);
}
```
