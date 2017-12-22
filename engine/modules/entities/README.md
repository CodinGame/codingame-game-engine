# About

The Entity Module takes care of displaying and animating graphical entities in the replay of the game, each frame of the viewer corresponds to a game turn.

Use it by creating shapes, sprites, texts etc, then commiting their states to a certain moment of each frame.

By default, the states are commited automatically at the end of a frame.

# Usage

Include the dependency below in the pom.xml of your project.
```xml
<dependency>
	<groupId>com.codingame.gameengine</groupId>
	<artifactId>module-entities</artifactId>
	<version>1.3</version>
</dependency>
```
Or a more recent version.

The EntityModule is an injectable Guice Singleton.

# Examples

## Creating a circle
```java
// Creates a green circle
Circle circle = entityModule.createCircle()
			.setRadius(50)
			.setLineWidth(0)
			.setFillColor(0x00FF00);
```
## Moving a circle
```java
MyPlayer player = gameManager.getPlayer(turn % 2);
circle
	.setX(player.getX())
	.setY(player.getY());
```
## Animating a circle
```java
//Starts invisible
circle.setRadius(0);
entityManager.commitEntityState(circle, 0);

//Grow to big size
circle.setRadius(70);
entityManager.commitEntityState(circle, 0.8);

//Shrinks to normal size
circle.setRadius(50);
entityManager.commitEntityState(circle, 1);
```

## Creating a group of sprites
```java
Sprite planet1 = entityManager.createSprite()
				.setImage("planet")
				.setX(-20);
Sprite planet2 = entityManager.createSprite()
				.setImage("planet")
				.setX(30);
				.setY(-10);
Sprite planet3 = entityManager.createSprite()
				.setImage("planet")
				.setY(20);

// The planets are around the point (960,540).
Group system = entityManager.createGroup(planet1, planet2, planet3)
					.setX(960)
					.setY(540);
```

## Spinning a group of spinning sprites around a point
```java
	planet1.setRotation(planet1.getRotation() - Math.PI / 4);
	planet2.setRotation(planet2.getRotation() + Math.PI);
	planet3.setRotation(planet3.getRotation() + Math.PI / 16);
	
	system.setRotation(system.getRotation() + Math.PI / 2);
	
	//Optional
	entityManager.commitWorldState(1);
```
	
# Documentation
	
## Reference API
	TODO