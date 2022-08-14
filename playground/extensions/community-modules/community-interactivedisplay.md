# InteractiveDisplayModule

Contributed by [Butanium](https://github.com/Butanium).

This module allows you to display entities when the mouse is over an entity or when

## Showcase

Here is a usage example <br>
<a href="https://live.staticflickr.com/65535/52235500282_9e6dfdbe65_o.gif"><img src="https://live.staticflickr.com/65535/52235500282_9e6dfdbe65_o.gif"/></a>

## Setup

⚠ This module requires
the [GraphicEntityModule](https://github.com/CodinGame/codingame-game-engine/tree/master/engine/modules/entities) to
work.

Add the dependency in the `pom.xml` of your project.

```xml

<dependency>
    <groupId>com.codingame.gameengine</groupId>
    <artifactId>module-interactivedisplay</artifactId>
    <version>${gamengine.version}</version> <!-- Must be higher than {todo : update the version once it's released} -->
</dependency>
```

Then setup the module in your `config.js`.

```javascript
import {GraphicEntityModule} from './entity-module/GraphicEntityModule.js'
import {InteractiveDisplayModule} from './interactivedisplay-module/InteractiveDisplayModule.js'

// List of viewer modules that you want to use in your game
export const modules = [
    GraphicEntityModule,
    InteractiveDisplayModule,
]
```

### Optional arguments

You can modify the module behavior by adding some lines in the `config.js`.

- Add this line to disable displaying entities when the mouse hover it

```js
InteractiveDisplayModule.enable_display_on_hover = false
```

- Add this line to allow processing multiple entities if they are all hovered

```js
InteractiveDisplayModule.allow_multiple_hover_display = true
```

- Add this line if you want to disable permanent display when an entity is clicked

```js
InteractiveDisplayModule.enable_display_on_click = false
```

- Add this line if you want to limit the number of clicked entities. If the limit is exceeded,
  the first clicked entities will be hide

```js
InteractiveDisplayModule.max_clicked_entities = 3
```

## Usage

`Referee.java`

```java
@Inject
InteractiveDisplayModule interactiveDisplayModule;

@Override
public void init(){
        // Add enities to display when the mouse is over entity
        interactiveDisplayModule.setDisplayHover(entity,entitiesToDisplay);
        // When you hide an entity that is registered in the module, untrack it to avoid visual bugs
        interactiveDisplayModule.untrack(entity);
        }
```
