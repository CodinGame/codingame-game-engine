# Stub Generator Syntax

## General 
The stub generator is the program which will write in every available language the default code you get when you open the IDE. To do this, the generator needs input written using the syntax below.

Each line of the **main block** must contain one of the following commands:

* `read <variable sequence>`
* `write <text>`
* `gameloop`
* `loop <amount> <command>`
* `loopline <amount> <variable sequence>`

A `<variable sequence>` is a sequence of space-separated variable declarations. Variable declaration are as follows:

```
[name]:[type]
```

where [name] is a valid variable name in **camelCase** and [type] is one of the following:

* `int`
* `float`
* `long`
* `word(<length>)`
* `string(<length>)`

⚠ A `string` can contain spaces, a `word` cannot. Therfore only one `string` may be used in the same sequence of variable declarations.

A `read` will collect a single line of data from the standard input.

A `loop` will perfom `<command>`, a read or a write, over `<amount>` lines. `<amount>` can be either a number or a variable name.

A `loopline` will collect `<amount>` times repeated data from a single line.

A `write` will print `<text>` onto the standard output. `<text>` can be a function `join()` which takes either plain text or variable names and joins them separated by spaces in the generated code.

## Comments

You may assign comments to any of the input variable by adding an **input block** one line away from the previous block.
```
read row:int col:int
write action

INPUT
row: The row of the grid containing your unit.
col: The column of the grid containing your unit.
```


You may assign comments to the unique `write` command by adding an **output block** one line away from the previous block.
```
read row:int col:int
write action

OUTPUT
MOVE <row> <col> to change the position of your unit
```


You may assign comments to the top of the code editor by adding a **statement block** one line away from the previous block.
This will override the default text which reads _"# Auto-generated code below aims at helping you parse the standard input according to the problem statement."_.
```
read row:int col:int
write action

STATEMENT
Move your unit around the grid and win the game!
```

## Example

### CodeBusters stub

```
read bustersPerPlayer:int
read ghostCount:int
read myTeamId:int
gameloop
read entities:int
loop entities read entityId:int x:int y:int entityType:int state:int value:int
loop bustersPerPlayer write join("MOVE", "8000", "4500")

STATEMENT
Send your busters out into the fog to trap ghosts and bring them home!

INPUT
bustersPerPlayer: the amount of busters you control
ghostCount: the amount of ghosts on the map
myTeamId: if this is 0, your base is on the top left of the map, if it is one, on the bottom right
entities: the number of busters and ghosts visible to you
entityId: buster id or ghost id
y: position of this buster / ghost
entityType: the team id if it is a buster, -1 if it is a ghost. 
state: For busters: 0=idle, 1=carrying a ghost. For ghosts: remaining stamina points. 
value: For busters: Ghost id being carried/busted or number of turns left when stunned. For ghosts: number of busters attempting to trap this ghost.

OUTPUT
MOVE x y | BUST id | RELEASE | STUN id
```

Produces:

```python
# Send your busters out into the fog to trap ghosts and bring them home!

busters_per_player = int(raw_input())  # the amount of busters you control
ghost_count = int(raw_input())  # the amount of ghosts on the map
my_team_id = int(raw_input())  # if this is 0, your base is on the top left of the map, if it is one, on the bottom right

# game loop
while True:
    entities = int(raw_input())  # the number of busters and ghosts visible to you
    for i in xrange(entities):
        # entity_id: buster id or ghost id
        # y: position of this buster / ghost
        # entity_type: the team id if it is a buster, -1 if it is a ghost.
        # state: For busters: 0=idle, 1=carrying a ghost. For ghosts: remaining stamina points.
        # value: For busters: Ghost id being carried/busted or number of turns left when stunned. For ghosts: number of busters attempting to trap this ghost.
        entity_id, x, y, entity_type, state, value = [int(j) for j in raw_input().split()]
    for i in xrange(busters_per_player):

        # Write an action using print
        # To debug: print >> sys.stderr, "Debug messages..."

        # MOVE x y | BUST id | RELEASE | STUN id
        print "MOVE 8000 4500"
```

### Nested loops

```
read n:int
loop 2 loop m read line:word(256)
```

Produces:

```python
n = int(input())
for i in range(2):
    for j in range(m):
        line = input()
```