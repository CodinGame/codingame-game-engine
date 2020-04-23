# Configuring your game

## General configuration

This section introduces configuration for any type of game.

For type-specific configuration, see:
- [Multiplayer Game Configuration](#multiplayer-game-configuration)
- [Solo Game Configuration](#solo-game-configuration)
- [Optimization Game Configuration](#optimization-game-configuration)

### pom.xml configuration

In order to use the CodinGame SDK, you need to correctly configure the file `pom.xml`.

A good start would be to check the [game skeleton' pom.xml](https://github.com/CodinGame/game-skeleton/blob/master/pom.xml). It contains all you need to start your project. Make sure you use the latest version of the SDK. See the [Release Notes](playground/misc/misc-3-release-notes.md).

Think of updating the `artifactId` value with the name of your game in case you want to create several games.

### config.ini

This is the main configuration file. It must be located in the `config` folder at the root of your project.

Example of `config.ini`.
```
type=multi
min_players=2
max_players=2
```
- **type**: The type of the game, must be either `multi`, `solo` or `opti`.
- **min_players**: The minimum number of players to play the game. Must be 1 for Solo and Optimization games, up to 8 for Multiplayer games.
- **max_players**: The maximum number of players to play the game. Must be 1 for Solo and Optimization games, up to 8 for Multiplayer games. Of course, you cannot have max_players &lt; min_players.

>This configuration can be done through a form when exporting the game, if the file `config.ini` is missing or misconfigured.

### Code Stub

You may add to the config/ folder a text file named `stub.txt`. The code stub provides a simple way to create code templates for players of your puzzle in multiple programming languages. If the contents of this file is a syntaxically valid **CodinGame Stub Generator** input, when the player opens your puzzle the IDE will be prefilled with input/output code.

An editor with preview of your `stub.txt` is available on `localhost:8888/stub.html`. This page allows you to edit your code stub and see the would-be generated input/output code side by side.

See [Stub Generator Syntax](https://www.codingame.com/playgrounds/40701/contribute---help/stub-generator-syntax) for details on how to write your code stub generator.

### Loading assets <a name="loading-assets"></a>

Assets are expected to be placed in the `src/main/resources/view/assets` folder of your game's project.

You can then use the images in the texture cache with the [Graphic Entity Module](playground/graphics/graphics-1-introduction.md):
```java
entityManager.createSprite.setImage("background.png");
```

### Other resources

You might need to upload other resources such as text files, csv or whatever your creativity will bring you to create.

Once you upload your game on [CodinGame](www.codingame.com), your project will not have the same file system as the one you use on your computer. In order to read your files, you must place them in the `src/main/resources` folder and use Java's `ClassLoader`:

```java
InputStream in = ClassLoader.getSystemResourceAsStream("my_awesome_file.txt");
```

### Levels & Leagues

In a **multiplayer** game, you have the possibility to create several levels (also named leagues). A new level allows you to set a different configuration and different rules (you can get the league level in the Referee with the [Game Manager](playground/core-concepts/core-4-game-manager.md)).

When the game will be released your levels will become leagues. The players will need to beat your Boss in the leaderboard to access the next league.

To create multiple levels, you need to make new folders named `level<number>` in the `config` directory. Their `<number>` must be positive and will be used to display your leagues in the right order. Each level can be configured like the `config` directory, which allow you to have different statements, stubs, etc.

If you want to use the same configuration in several levels, you do not need to copy your files in every directory. If a file is missing in a `level` folder, it will inherit from `config` automatically when uploading your game.

Example of a `config` directory structure:
```
.
├── config
│   ├── level1
│   │   ├── statement_en.html
│   │   └── Boss.java
│   ├── level2
│   │   └── statement_en.html
│   ├── level3
│   │   ├── statement_en.html
│   │   └── config.ini
│   ├── Boss.java
│   ├── config.ini
│   └── stub.txt
```

Please note that you **cannot** have different types of game in your levels. Inconsistent `config.ini` files will cause unspecified behaviour.

### Viewer configuration <a name="viewer-configuration"></a>

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

You can change the identifier of your game for the online IDE's cache by adding an export to `config.js`:
```javascript
export const gameName = 'MyGame';
```
This doesn't have any effect in the local test page. It is used to store the viewer options, such as player speed, selected by the user.

### Statement

All games need to have a description in the form of a game statement. A statement in English is mandatory, and one in French is optional. The `statement_en.html` already exists in the config/ folder, and you may create a `statement_fr.html` file if you wish. If the player has set French as his language on CodinGame, the French version of the statement will be displayed; otherwise the English version will be displayed.

For a game with multiple leagues, you may place a file named `statement_en.html.tpl` in the `config/` directory and it will be used as a basis for the statement of each league when you click the export button. Within the `.tpl` file, you may place special comment blocks to indicate whether a block of html should be included for any specified league.

An editor with preview of your `statement_en.html` and `statement_fr.html` is available on `localhost:8888/statement.html`. This page allows you to edit your html files and see the would-be generated statement side by side.

Learn more about how to write a statement for sdk games in the [CodinGame contribution documentation](https://www.codingame.com/playgrounds/40701/contribute---help/writing-the-statement#game-statement)

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


### Activating game logs

You can view the data that the Referee and the Players send each other by editing the built-in logger's settings.
To do this, open `log4j2.properties` in `src/test/ressources` and replace ```rootLogger.level = warn``` with ```rootLogger.level = info```.
Additionally, if you would like to see the output of the different modules, you can use:
`rootLogger.level = trace`.


# Multiplayer Game Configuration <a name="multiplayer-game-configuration"></a>


### Bosses

Multiplayer games require bosses to battle against. They will be your opponent by default in the CodinGame IDE and players will need to beat them in the leaderboard to reach the next league (if present).

Your Boss can be coded in any of the languages supported by CodinGame. Make sure you use the right extension using the following table:

::: Supported Languages

|  Language  | Extensions |
| ---------- | ---------- |
| Clojure    | .clj, .cljs |
| Swift      | .swift |
| Rust       | .rs |
| Dart       | .dart |
| Pascal     | .pas, .p |
| Groovy     | .groovy |
| Lua        | .lua |
| C++        | .cpp |
| OCaml      | .ml, .mli |
| ObjectiveC | .m, .mm |
| Scala      | .scala |
| Java       | .java |
| Javascript | .js, .jsm, .jsx |
| Python3    | .py .py3 |
| PHP        | .php, .php3, .php4, .php5, .phps, .phpt |
| Kotlin     | .kt |
| Bash       | .sh |
| Go         | .go |
| C          | .c |
| Haskell    | .hs |
| Perl       | .pl, .pm |
| Ruby       | .rb, .ru |
| Python     | .py |
| F#         | .fs |
| VB.NET     | .vbs, .vb |
| C#         | .cs |

:::

Your Boss should be coded as if it could be used in the IDE. It is suggested that you start coding your AI on the base of the code generated by your stub.

### Welcome popup

Welcome popups can be used in **Multiplayer** games with multiple leagues. They will be displayed when a player is promoted to the next league.

Place a file named `welcome_en.html` in every `config/level<number>` directory you want the popup to be used in.

You can display images using `<img src="your_image.jpg"/>`. The image files must be located in the same directory.

Optionally, you can tranlate your popups in French using `welcome_fr.html` files.


# Solo Game Configuration <a name="solo-game-configuration"></a>


### Test case file <a name="test-case-file"></a>

You will need to create test case files to run your **Solo** game.

Your test cases must be named `test<number>.json` or `test<number>.yaml` and placed in the `config` directory. Their `<number>` determine the order they will be listed in the CodinGame IDE. Here is an example:

`test1.json`
```json
{
	"title": {
		"2": "One path",
		"1": "Un seul chemin"
	},
	"testIn": ".o...\n.ooo.\n...o.",
	"isTest": "true",
	"isValidator": "false"
}
```

or the YAML equivalent:

`test1.yaml`
```yaml
title:
  2: One path
  1: Un seul chemin
testIn: |-
  .o...
  .ooo.
  ...o.
isTest: 'true'
isValidator: 'false'
```

- **title:**
    - **2:** English title, this parameter is mandatory.
    - **1:** French title, optional.
- **testIn:** The content of your test case. It can contain multiple lines separated with `\n`.
- **isTest:** If true, this test will be visible and used as a regular test case.
- **isValidator:** If true, this test will be used to validate the player's code. You can use this to avoid hardcoded solutions and calculate Optimization games scores.


# Optimization Game Configuration <a name="optimization-game-configuration"></a>


**Optimization** games are **Solo** games with additional configuration.

First, you will need to add a `criteria` and a `sorting_order` property in `config.ini`.
- The `criteria` corresponds to the label of the player's score. For example, it can be `Points`, `Fuel` or `Distance`.
- The `sorting_order` determines the ranking order. Its value must be either `asc` or `desc`. If the player whose `Fuel` quantity is higher should be first, choose `desc`. If the goal is to win the game with the least `Distance`, choose `asc`.

You can also choose to translate your criteria in French by using the optional properties `criteria_en` and `criteria_fr`.

>This configuration can be done through a form when exporting the game, if the file `config.ini` is missing or misconfigured.

Once this configuration is done, you will need to send your player's score with the [Game Manager](core-3-game-manager.md#optimization-game-features)
