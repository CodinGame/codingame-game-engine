# Release notes

The CodinGame SDK is regularly updated and improved. This document lets you know what changed in the latest releases.

## 4.1.5

### 🐞 Bug fix

- Fixed `TilingSprite` _Contributed by [Butanium](https://github.com/Butanium)_
- Fixed `CameraModule` bug. _Contributed by [Butanium](https://github.com/Butanium)_

## 4.1.4

### 🐞 Bug fix

- Updated statement html sanitizer.
- Various fixes on the `CameraModule`. _Contributed by [Butanium](https://github.com/Butanium)_

## 4.1.3

### 🎁 Features

- First community-made module: `CameraModule`.

### 🐞 Bug fix

- Updated statement html sanitizer for a more recent css property whitelist.

## 4.1.2

### 🎁 Features

- PIXI updated from v5.3.8 to v6.0.2

### 🐞 Bug fix

- Fixed missing texture errors no longer being caught

### 📝 Refactoring

- Removed unused code
- Removed link to littera tool from docs

## 4.0.2

### 🐞 Bug fix

- When exporting a game, folders named "node_modules" are now ignored.
- Renamed viewer modules' `name` variable to `moduleName` for consistency and future-proofing.

### ⚠️ Known issues

- Missing texture errors are no longer reported


## 4.0.1

⛔ Broken version

## 4.0.0

### 🎁 Features

- PIXI updated from v4.8.5 to v5.3.8

## 3.15.6

### 🐞 Bug fix

- Fixed a bug in the TooltipModule causing tooltips to stay visible permanently.
- BufferedGroup will now dynamically resize to fit contents.
- Fixed outstanding CSS issues in the statement preview page.

## 3.15.5

### 🐞 Bug fix

- Fixed viewer sometimes crashing on startup.

## 3.15.4

### 🐞 Bug fix

- Added a missing chunk of CSS from the statement preview page.


## 3.15.3

### 🐞 Bug fix

- The statement preview page now presents HTML as it would show in the CodinGame IDE.
- The statement preview page now looks for both `.html` and `.html.tpl` files.
- Fixed `ToggleModule` taking up a lot of characters in the game result from the allowed quota.


## 3.15.2

### 🐞 Bug fix

- `TooltipModule` would sometimes show an empty tooltip block
- Fixed `TooltipModule` taking up a lot of characters in the game result from the allowed quota
- Improved documentation and usage of `GameRunner.simulate()` and `GameResult`

## 3.15.1

### 🐞 Bug fix

- Lines were incorrectly drawn if only one of their two points were changed

## 3.15.0

### 🎁 Features

- Updated the stub generator library to version 2.23.1 (adds support of bool type)

### 🐞 Bug fix

- Fix player outputs size lower than expected number of lines when last lines are empty strings

## 3.14.0

### 🎁 Features

- Improve statement editor
- Preview statement for the various leagues, when available, in the statement editor
- Player one and player two's default avatars are now the robot and the alien

## 3.13.1

### 🐞 Bug fix

- Fixed CSS issues in statement editor preview
- Texts no longer parse nickname tokens for player ids that are out of range

## 3.13.0

### 🎁 Features

- `Entity` now exposes the `getParent` method to acces the container an entity has been added into

### 🐞 Bug fix

- An exception is now thrown when one tries to add the same entity to two different groups
- `Group::remove` now functions correctly
- Fixed display errors when dragging the player cursor to the first frame of a game.

## 3.12.0

### 🎁 Features

- It is now possible to set the maximum width of a Text or BitmapText. If the string is too long, it will be shortened until it fits the given width and will include an ellipsis. This is useful when the text length is unknown during runtime, such as using the player's username token.

### 🐞 Bug fix

- Fixed an issue where games with a `stepByStepAnimateSpeed` would sometimes try to animate frame 0, causing a black screen.
- The `GraphicEntityModule` method `createText` no longer requires a text parameter

## 3.11.1

### 🐞 Bug fix

- Fixed choppy animation when launching games with a `stepByStepAnimateSpeed`
- Fixed situations in which stepping through a game with a `stepByStepAnimateSpeed` would not be animated.
- Remove faulty code that was supposed to ensure all lines were at least 1 pixel thick. User now has greater control over line thickness.

## 3.11.0

### 🎁 Features

- Added text-align property to TextBasedEntity
- SpriteSheetSplitter splits using full width and/or height of spritesheet when width and/or height not set.

### 🐞 Bug fix

- Changed CSS of statement preview to match the codingame IDE
- Fixed bug where newline in Text entities would crash the game
- Fixed missing display of the agent's standard error stream on crash

## 3.10.1

### 🐞 Bug fix

- Removed accidental console.log from app.js
- Fixed `SpriteAnimation` that would sometimes not animate

## 3.10.0

### 🎁 Features

- Added game statement editor with preview

### 🐞 Bug fix

- Report malformed JSON error instead of crashing when there are invalid JSON files in assets folder
- Fixed game replay where output always shown at player 0 for games in which not all players act each turn

## 3.9.0

### 🎁 Features

- Added EASE_IN and EASE_OUT curves

## 3.8.4

### 🐞 Bug fix

- Fixed a small issue with the loading of avatars
- Fixed crashes when the same font asset is included twice
- Updated the stub generator library

## 3.8.3

### 🐞 Bug fix

- SpriteAnimation: fix wrong current frame when the animation duration is changed

## 3.8.2

### 🐞 Bug fix

- ViewportModule: don't reset zoom when playing

## 3.8.1

### 🎁 New features

- The [ViewportModule](playground/extensions/extensions-6-viewport.md) has been added

## 3.8.0

⛔ Broken version

## 3.7.0

### 🎁 New features

- Add possibility to edit and preview the stub: http://localhost:8888/stub.html

### 🐞 Bug fix

- Referee output now properly displayed
- Player output now properly displayed
- Game Errors now displayed once more

## 3.6.1

### 🐞 Bug fix

- Fix the `stepByStepAnimateSpeed` getting interrupted by clicking in the IDE
- Fix YAML testcases export

### ⚠️ Known issues

- Referee and player output improperly displayed
- Game Errors not displayed

## 3.6.0

### 🎁 New features

- Add `TilingSprite`

### ⚠️ Known issues

- Referee and player output improperly displayed
- Game Errors not displayed

## 3.5.2

### 🐞 Bug fix

 - Fixed the `skewX` and `skewY` interpolation method.
 - Fixed the next frame button no longer updating the view

## 3.5.1

### 🐞 Bug fix

 - Fixed the `skewX` and `skewY` properties for all `Entities`.

### ⚠️ Known issues

- `skew` property is not functional, to use it please move to 3.5.2 or higher
- the next frame button no longer updates the view

## 3.5.0

### 🎁 New features

- Added methods to change an entity's `skew` in the `GraphicEntityModule`
- Added methods to change the player timeout of the first round of a game.

### 🐞 Bug fix

 - Updated the test page's player to match the codingame IDE

### ⚠️ Known issues

- `skew` property is not functional, to use it please move to 3.5.2 or higher
- the next frame button no longer updates the view

## 3.4.10

### 🎁 New features

- Added an optional export to `config.js` called `stepByStepAnimateSpeed` which lets you set whether to animate the view when stepping from one frame to the next

### 🐞 Bug fix

 - Fixed Tooltip not showing all texts of stacked sprites below the cursor
 - Fixed viewer not updating on mouse wheel event

## 3.4.9

### 🐞 Bug fix

- Invalid negative values for colours no longer crash the game
- Fixed missing line breaks in the `Game Summary` console
- Frames now start at `0` in local test page
- Better error reporting on player agent crash
- Multiple commands can now be launched with `CommandLinePlayerAgent`
- Games now require less RAM in the CodinGame IDE

## 3.4.8

### 🐞 Bug fix

- The `turn` argument of the Referee's `gameTurn()` now starts at 1 instead of 0
- A Text can now have semicolons in it
- Improved error handling


## 3.4.7

### 🐞 Bug fix

- Fixed `ToggleModule` and improved its API.

## 3.4.6

### 🐞 Bug fix

- The intro replay file `demo.js` is now smaller when generated from scratch.
- Improve game load performance.

### ⚠️ Known issues

- `ToggleModule` only works in local IDE

## 3.4.5

### 🐞 Bug fix

- `EndScreenModule` now compatible with PIXI v4.8.5

### ⚠️ Known issues

- `ToggleModule` only works in local IDE

## 3.4.4

### 🐞 Bug fix

- Improved error handling.

### ⚠️ Known issues

- `ToggleModule` only works in local IDE

## 3.4.3

### 🐞 Bug fix

- The `EndScreenModule` now properly handles more than two players.

### ⚠️ Known issues

- `ToggleModule` only works in local IDE

## 3.4.2

### 🐞 Bug fixes

- `Circle` entities now displayed again
- Included missing `setLeagueLevel` method in the `GameRunner`

### ⚠️ Known issues

- `ToggleModule` only works in local IDE

## 3.4.1

### 🎁 New features

- Added `RoundedRectangle` Shape
- Added `Polygon` Shape

### 🐞 Bug fix

- Better display of game errors

### 📒 Notes

- PIXI updated from v4.4.3 to v4.8.5

### ⚠️ Known issues

- `Circle` entities no longer displayed
- `ToggleModule` only works in local IDE

## 3.4.0

⛔ Broken version

## 3.3.2

### 🐞 Bug fixes

- Included missing `addAgent` polymorphism – custom nickname with default avatar.
- Improved display of player output in local test page.
- Better handling of errors from initializing modules.
- Unused `title` property no longer mandatory in `config.ini`.
- `setFrameDuration()` now throws an exception on non-positive values.
- We no longer wait the full player timeout time when creating a frame when no players have been executed.

### ⚠️ Known issues

- `ToggleModule` only works in local IDE

## 3.3.1

### 🐞 Bug fix

- `setFrameDuration()` regression from 3.0.0 fixed.

### ⚠️ Known issues

- Frames created when no players have been executed will still wait the full player timeout time.
- `ToggleModule` only works in local IDE

## 3.3.0

### 🎁 New feature

- The [EndScreenModule](playground/extensions/extensions-4-endscreen.md) allows you to display a custom text instead of the score.

### ⚠️ Known issues

- Frames created when no players have been executed will still wait the full player timeout time.
- `ToggleModule` only works in local IDE

## 3.2.0

### 🎁 New features

- The [EndScreenModule](playground/extensions/extensions-4-endscreen.md) and the [TooltipModule](playground/extensions/extensions-3-tooltip.md) are now bundled with the sdk.
- The [ToggleModule](playground/extensions/extensions-toggle.md) has been added.

### 🐞 Bug fix

- Fixed absence of game summaries from the local test page of SOLO games.

### ⚠️ Known issues

- Frames created when no players have been executed will still wait the full player timeout time.
- Maven modules not available as dependencies
- `setFrameDuration()` does not work in the `init()`.
- `ToggleModule` only works in local IDE

## 3.1.0

### 🎁 New feature

- A frame is created even if no players have been executed at the end of each gameTurn.

### 🐞 Bug fix

- Fixed an issue with sprites.

### ⚠️ Known issues

- Frames created when no players have been executed will still wait the full player timeout time.
- `setFrameDuration()` does not work in the `init()`.

## 3.0.0
*January 11, 2019*

### 🎁 New features

- [BitmapText](playground/graphics/graphics-text.md#BitmapText) has been added.
- [Text](playground/graphics/graphics-text.md#Text) can be bold.
- Shapes now have a blendmode property.

### 🐞 Bug fixes

- 💥 _Breaking change_ Renamed SpriteSheetLoader to SpriteSheetSplitter.
- Subfolders of the assets folder work on Windows now.
- Frame zero now has a duration of zero.
- The [SpriteSheetSplitter](playground/graphics/graphics-4-spritesheets.md#SpriteSheetSplitter) doesn't reload the sprite sheet if it's already in the TextureCache.
- The local player now uses the parameters stored in the LocalStorage.
- Removed the max turn warning, since it's replaced by a limit of alloted time.
- Better process cleanup.
- Fixed security issue (ZipSlip bug).
- Fixed the minimum max time alloted to players (now it really is 50ms).


### ⚠️ Known issues

- `setFrameDuration()` does not work in the `init()`.

## 2.15
*November 29, 2018*

### 🐞 Bug fixes

- Frame zero is not animated anymore.
- Less spammy warnings.

## 2.14
*October 30, 2018*

### 🎁 New features

- Limited the max total alloted time to the players to 25s.
- SpriteAnimation now a has reworked API and a pause function.

### 🐞 Bug fixes

- Better error handling.
- Reworked the commit system, fixing some big issues and reducing data usage.

## 2.11
*September 14, 2018*

### 🐞 Bug fixes

- Fixed BufferedGroup performance issues.
- Better handling of modules errors.

## 2.9
*August 21, 2018*

### 🎁 New feature

- Local test page design now matches the website one.

## 2.8
*August 9, 2018*

### 🐞 Bug fixes

- Reworked the Test case API.
- Fixed some WEBGL memory leaks.

## 2.7
*August 1, 2018*

### 🐞 Bug fixes

- Handling of the absence of a logo in the demo.
- Limiting the size of the game summary.

## 2.5
*July 6, 2018*

### 🎁 New feature
 - Game Params are now displayed in the local test page.

## 2.4
*June 25, 2018*

### 🎁 New feature

- [Buffered Groups](playground/graphics/graphics-6-advanced.md#buffered-groups) have been added.

### 🐞 Bug fixes

- Test cases can now handle several lines as input.
- The configuration verification does not check the presence of `welcome_en.html` in the first league anymore. It also concerns games with no league system.

## 2.3
*June 8, 2018*

### 🐞 Bug fixes

- The `BindException` thrown when running a new instance of the game when the server is already on use has been caught. It now logs a warning.
- A bug would prevent you from exporting a game that was too heavy. It is now fixed.

## 2.2
*June 4, 2018*

### 🐞 Bug fixes

- Solo games agents not having colors would produce an error once uploaded on CodinGame. A default color has been added.
- A regression would prevent the configuration form from being displayed. This feature is back.

## 2.1
*June 1, 2018*

### Bug fix

- The Graphic Entity Module would stop displaying the last frame of a replay. This is now fixed.

## 2.0
*June 1, 2018*

### 🎁 New features

- [Solo](playground/getting-started/tutorial-3-solo.md) and [Optimization](playground/getting-started/tutorial-4-opti.md) games have been added.
    - Implementation of Multiplayer and Solo classes extending from formerly used `AbstractPlayer`, `GameManager` and `GameRunner`. These features are *not* backward compatible.
    - [Test cases](playground/core-concepts/core-4-configuration.md#test-case-file) have been added.
    - The configuration verification has been updated to match the new constraints.
    - The form to set up basic configurations when exporting the game has been updated. It now handles the different type of games and specific settings for each of them.

## 1.37 and older versions

These versions are not handled anymore and your game will not work if you upload it on CodinGame. If you are still using this version, it is strongly recommended to update your project to the latest release.
