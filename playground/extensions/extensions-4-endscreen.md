# EndScreenModule

The module is bundled with the version 3.2.0 of the sdk or higher.

Can be used to display the ranking of a multiplayer game with any additional info you choose. The ranking will appear at the very end of the replay.

## Import

Add the dependency in the `pom.xml` of your project.
```xml
<dependency>
	<groupId>com.codingame.gameengine</groupId>
	<artifactId>module-endscreen</artifactId>
	<version>3.4.1</version>
</dependency>
```
And load the module in your `config.js`.
```javascript
import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { EndScreenModule } from './endscreen-module/EndScreenModule.js';

export const modules = [
	GraphicEntityModule,
	EndScreenModule
];
```

## Usage

To guarantee the correct ranking, you must set this module's score property in your Referee's `onEnd()` method.

`Referee.java`
```java
  @Override
  public void onEnd() {
    endScreenModule.setScores(gameManager.getPlayers().stream().mapToInt(p -> p.getScore()).toArray());
  }
```

The module loads by default your `logo.png` as title, you can set your own image with `setTitleRankingsSprite()`.
```java
  endScreenModule.setTitleRankingsSprite("myCustomSprite.png");
```

You can also display a custom text instead of the score.

`Referee.java`
```java
  @Override
  public void onEnd() {
    int[] scores = { player1.getScore(), player2.getScore() };
    String[] text = { scores[0] + " mana", scores[1] + " mana" };

    endScreenModule.setScores(scores, text);
  }
```