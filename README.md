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


### Code Stub

You may add to the config/ folder a text file named `stub.txt`. If the contents of this file is a syntaxically valid **CodinGame Stub Generator** input, the IDE will be prefilled with input/output code.

See [Stub Generator Syntax](stubGeneratorSyntax.md) for details.


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


### Statement

Place a file named `statement_en.html` in the `config/` directory and it will be used for as the statement of your game.

For a game with multiple leagues, you may place a file named `statement_en.html.tpl` in the `config/` directory and it will be used as a basis for the statement of each league when you click the export button.

Within the `.tpl` file, you may place special comment blocks to indicate whether a block of html should be included for any specified league.

#### Example

This `statement_en.html.tpl`:
```html
<!-- LEAGUES level1 level2 level3 -->
<div>
  <p> Always included </p>
  <!-- BEGIN level1 -->
    <p> Included in first league </p>
  <!-- END -->
  <!-- BEGIN level2 -->
    <p> Included in second league </p>
  <!-- END -->
  <!-- BEGIN level1 level2 -->
    <p> Included both first and second league </p>
  <!-- END -->
</div>
```

Will result in these three statements for a three-league game:

First league in `config/level1/statement_en.html`:
```html
<div>
  <p> Always included </p>
    <p> Included in first league </p>
    <p> Included both first and second league </p>
</div>
```

Second league in `config/level2/statement_en.html`:
```html
<div>
  <p> Always included </p>
    <p> Included in second league </p>
    <p> Included both first and second league </p>
</div>
```

Third league in `config/level3/statement_en.html`:
```html
<div>
  <p> Always included </p>
</div>
```


## Welcome popup

Welcome popups can be used in **Multiplayer** games with multiple leagues. They will be displayed when a player is promoted to the next league.

Place a file named `welcome_en.html` in every `config/level<number>` directory you want the popup to be used in.

You can display images using `<img src="your_image.jpg"/>`. The image files must be located in the same directory.