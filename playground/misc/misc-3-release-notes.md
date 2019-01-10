# Release notes

The CodinGame SDK is regularly updated and improved. This document lets you know what changed in the latest releases.

## 2.4
*June 25, 2018*

### New feature

- [Buffered Groups](graphics/graphics-6-advanced.md#buffered-groups) have been added.

### Bug fixes

- Test cases can now handle several lines as input.
- The configuration verification does not check the presence of `welcome_en.html` in the first league anymore. It also concerns games with no league system.

## 2.3
*June 8, 2018*

### Bug fixes

- The `BindException` thrown when running a new instance of the game when the server is already on use has been caught. It now logs a warning.
- A bug would prevent you from exporting a game that was too heavy. It is now fixed.

## 2.2
*June 4, 2018*

### Bug fixes

- Solo games agents not having colors would produce an error once uploaded on CodinGame. A default color has been added.
- A regression would prevent the configuration form from being displayed. This feature is back.

## 2.1
*June 1, 2018*

### Bug fix

- The Graphic Entity Module would stop displaying the last frame of a replay. This is now fixed.

## 2.0
*June 1, 2018*

### New features

- [Solo](getting-started/tutorial-3-solo.md) and [Optimization](getting-started/tutorial-4-opti.md) games have been added.
    - Implementation of Multiplayer and Solo classes extending from formerly used `AbstractPlayer`, `GameManager` and `GameRunner`. These features are *not* backward compatible.
    - [Test cases](core-concepts/core-4-configuration.md#test-case-file) have been added.
    - The configuration verification has been updated to match the new constraints.
    - The form to set up basic configurations when exporting the game has been updated. It now handles the different type of games and specific settings for each of them.

## 1.37 and older versions

These versions are not handled anymore and your game will not work if you upload it on CodinGame. If you are still using this version, it is strongly recommended to update your project to the latest release.