# Graphics

The graphics of a game created with the CodinGame SDK are handled with the Javascript framework [PixiJS](http://www.pixijs.com/). This section introduces the main features of the Graphic Entity Module, a Java module that you can use to interact with the viewer.

This module takes care of displaying and animating graphical entities in the replay of the game, each frame of the viewer corresponds to a game turn.

Use it by creating shapes, sprites, texts etc, then commiting their states to a certain moment of each frame.

By default, the states are commited automatically at the end of a frame.

# Usage

Include the dependency below in the pom.xml of your project.
```xml
<dependency>
	<groupId>com.codingame.gameengine</groupId>
	<artifactId>module-entities</artifactId>
	<version>3.4.1</version>
</dependency>
```
Or a more recent version. See the [Release Notes](playground/misc/misc-3-release-notes.md).

The GraphicEntityModule is an injectable Guice Singleton.

# Table of contents

- Create and manipulate [Entities, Shapes and Sprites](graphics-2-entities.md).
- Understand how to [interact with the viewer](graphics-3-states.md).
- See how to use [Spritesheets](graphics-4-spritesheets.md).
- Start [Animating](graphics-5-animations.md) your game!
- Comfortable with the module? Check [Advanced features](graphics-6-advanced.md).

# Go further and create your own modules

Using this module is the easiest way to draw your graphical entities in the viewer. Although it provides many features, you might want to add your own. See the [SDK extensions](playground/extensions/extensions-1-tools.md) to get help starting!