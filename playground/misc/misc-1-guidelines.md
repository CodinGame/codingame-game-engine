# Guidelines

When creating your game, we suggest you follow these guidelines in order to improve both its performance and players experience.

## Technical Guidelines

- The game should be noob friendly. Can make a workable AI with a bunch of ifs. 
- If it is a game with limited output options per turn, send those options in the protocol (e.g. the first player of chess has 20 possible actions).
- Viewer should be enough to understand most of the rules.
- Gameplay based around rounds/turns. Only perform checks (collision, presence, etc.) at the end of a turn.
- End player’s program when it outputs an unrecognised command. Continue player’s program when it outputs a recognised but invalid command and add error message to summary.
- Stop game early if players aren’t doing anything or are otherwise stalemated.
- Stop game early if the ranking is decided whatever the players may do.
- Always minify png and jpg assets.
- The Referee must not crash, make sure all possible exceptions are caught, especially during the parsing of a Player’s actions, where most errors occur.
- Minimum font size: 36px.

## Soft Limits

The following limits will make your game run faster, but they are not strict limitations.

- Statement length: less than 6k characters
- Total entity creations: less than 200.
- Max timeout: 50ms per round
- Max rounds: 250 rounds.

## Protocol:

The following lines will help most players to get into your game. Try to simplify things when possible. 

- Two loops max for input.
- Variables to read per loop: less than 10.
- Stub generator must not exceed: 45 lines of Java.
- If the player is given an ID, it is always 0.

## Graphics:

- Privilege the use of Sprites over Shapes (Rectangle, Circle, etc.)
- Use less than 2Mb total for assets.

## Games with leagues

- Stubs should, if possible, be the same for every league.
- If a game needs at least 1 data structure to make an AI that doesn’t crash, it needs an easier league.

## Rules

When contributing on CodinGame, you must accept and apply these rules:

- Your game must not contain any kind of inappropriate content.
- Write a stub generator. The documentation can be found in the [CodinGame SDK repository](https://github.com/CodinGame/codingame-game-engine/blob/master/stubGeneratorSyntax.md). Any non-explicit variable must be explained with a comment.
