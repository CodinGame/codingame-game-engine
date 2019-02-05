# Extend the CodinGame SDK

Although the CodinGame SDK already includes the [GraphicEntityModule](graphics-1-introduction.md), you can add modules to your game. Modules are extensions allowing you to directly interact with the viewer using [PixiJS](http://www.pixijs.com/), which gives you a lot more control on its behaviour.

If you want to create your own module, see [How to get started](extensions-2-tutorial.md).

# Usage

## Importing the module

### Modules on maven

You will need to include the module dependency in the pom.xml of your project.

You will also need to import your module to the `src/main/resources/view/config.js` file, in the `modules` array.

Example of a project using the Graphic Entity Module and the End Screen Module:
`pom.xml`
```xml
<dependency>
	<groupId>com.codingame.gameengine</groupId>
	<artifactId>module-entities</artifactId>
	<version>3.4.1</version>
</dependency>
<dependency>
	<groupId>com.codingame.gameengine</groupId>
	<artifactId>module-endscreen</artifactId>
	<version>3.4.1</version>
</dependency>
```
`config.js`
```javascript
import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { EndScreenModule } from './endscreen-module/EndScreenModule.js';

export const modules = [
	GraphicEntityModule,
	EndScreenModule
];
```

### Custom modules

Custom modules usually come up with a `.java` and a `.js` file. Place these files in `src/main/java` and `src/main/resources/view` respectively.

You will also need to import your module to the `src/main/resources/view/config.js` file, in the `modules` array.

Example of a project using the Graphic Entity Module and a custom module:
`config.js`
```javascript
import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { CustomModule } from './modules/custom/CustomModule.js';

export const modules = [
	GraphicEntityModule,
	CustomModule
];