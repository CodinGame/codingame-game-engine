# About

This is the Game Engine Toolkit of CodinGame. With it one can develop a game for the CodinGame platform.

This engine is meant to be imported with maven from a project such as [the Game Engine skeleton](https://github.com/CodinGame/game-skeleton).

# Getting started

Check the documentation on the [github repository](https://github.com/CodinGame/codingame-sdk-doc).

# Usage

## Using the Game Runner

See [Game Runner readme](runner/).

## Using the Game Manager

See [Game Manager readme](engine/core/).

## Using the Graphic Entity Module

See [Graphic Entity Module readme](engine/modules/entities/).

## Configuring your game

### Viewer configuration

You can change the default player colors to whatever you wish by adding an export to `config.js`:
```javascript
export const playerColors = [
      '#ff1d5c', // radical red
      '#22a1e4', // curious blue
      '#ff8f16', // west side orange
      '#6ac371', // mantis green
      '#9975e2', // medium purple
      '#3ac5ca', // scooter blue
      '#de6ddf', // lavender pink
      '#ff0000'  // solid red
    ];
```
The colors must respect the above format.

You can change the identifier of your game for the IDE's cache by adding an export to `config.js`:
```javascript
export const gameName = 'MyGame';
```
This doesn't have any effect on user experience yet.


### Code Stub

You may add to the config/ folder a text file named `stub.txt`. If the contents of this file is a syntaxically valid **CodinGame Stub Generator** input, the IDE will be prefilled with input/output code.

See [Stub Generator Syntax](stubGeneratorSyntax.md/) for details.

