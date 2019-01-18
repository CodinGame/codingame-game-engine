# Animating entities

In order for your entities to be animated in the viewer, you must commit your entities state. You do not need to do it manually as the module **automatically commits all the entities at the end of each frame**.

However, you might want to force a commit, for example, to create several animations of the same entity during a single frame.

# Usage

Two methods allow you to commit your entities:
```java
commitEntityState(double t, Entity<?>... entities);
```
will commit the state of `entities` at the moment `t` (0 ≤ `t` ≤ 1 ).
0 being the start of the frame and 1 the end of the frame.

```java
commitWorldState(double t);
```
will commit the state all the entities you created at the moment `t` (0 ≤ `t` ≤ 1).

If you want to commit a high amount of entities you may consider using commitWorldState instead of commitEntityState for better performances.

# Examples

## Animating a circle
```java
//Starts invisible
circle.setRadius(0);
graphicEntityModule.commitEntityState(0, circle);

//Grow to big size
circle.setRadius(70);
graphicEntityModule.commitEntityState(0.8, circle);

//Stays with the same size a little bit
graphicEntityModule.commitEntityState(0.9, circle);

//Shrinks to normal size
circle.setRadius(50);
graphicEntityModule.commitEntityState(1, circle);
```
It should look like this :
![Example](resources/frames.gif)

## Animating several entities

```java
circle.setRadius(50);
line.setX(30);
rectangle.setAplha(0.67);
commitWorldState(0.5);

circle.setRadius(70);
line.setX(50);
rectangle.setAplha(1);
commitWorldState(1);
```
