# Extend the CodinGame SDK

Although the CodinGame SDK already includes the [GraphicEntityModule](graphics-1-introduction.md), you can add modules to your game. Modules are extensions allowing you to directly interact with the viewer using [PixiJS](http://www.pixijs.com/), which gives you a lot more control on its behaviour.

This section presents a few existing modules. You can find their source code on Github: [https://github.com/CodinGame/codingame-sdk-modules](https://github.com/CodinGame/codingame-sdk-modules)

If you want to create your own module, see [How to get started](extensions-2-tutorial.md).

# Usage

Modules usually come up with a `.java` and a `.js` file. Place these files in `src/main/java` and `src/main/resources/view` respectively.

You will also need to import your module to the `src/main/resources/view/config.js` file, in the `modules` array.

Example of a project using the Graphic Entity Module and the End Screen Module:
```javascript
import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { EndScreenModule } from './modules/endscreen/EndScreenModule.js';

export const modules = [
	GraphicEntityModule,
	EndScreenModule
];
```

To understand how to edit modules, see the examples:
- [Tooltip Module](extensions-3-tooltip.md)
- [End Screen Module](extensions-4-endscreen.md)
- [Anim Module](extensions-5-animmodule.md)