# Card Game

A multi-threaded card game simulation in Java. Each of the n players runs on
its own thread, with players and decks forming a ring: player i draws from deck i
and discards to deck i+1 (but player n discards to deck 1).

Pair-programming coursework for ECM2414.
Authors: Aidan Taylor and Allistair Wallman.

## Installation

### Requirements

JDK 17 
Maven 3.9.x
JUnit 5 (Jupiter) 5.11.4

JUnit is the only dependency and is declared in `pom.xml`.
Maven downloads it on the first build, so internet access is needed the
first time. The game only uses the Java standard library.

Use commands below to check your setup:
```bash
java -version
mvn -version
```

### Build

```bash
mvn clean package
```
This compiles all code, runs tests and creates `target/cards.jar`. To skip
the tests: `mvn clean package -DskipTests`.

## Usage

```bash
java -jar cards.jar
```
The program asks for:
The number of players `n`f: which must be a positive integer (not including 0).
The pack file path: which must be a plain text file with exactly `8n` lines, each one a single non-negative integer

Example, using the included `sample-pack.txt` (32 cards, for n = 4):
Example input shown below:
```
Enter a positive number of players:
4
Enter the pack filepath:
sample-pack.txt
```

### Output

All output files are written to the current working directory and overwrite
any existing files with the same names:

`player1_output.txt`: initial hand, each draw, discard and current hand, then the result and final hand
`deck1_output.txt`: final contents of each deck (deck sizes may differ at the end; this is expected)

The winner is also printed to the terminal as `player X wins`.

### Game rules implemented
- Hands of four cards are dealt round-robin from the pack, then the decks are
  filled round-robin from the rest of the cards.
- A player wins with four cards of the same value. An initial winning hand ends
  the game before any thread starts.
- Each turn, a player draws from the top of their deck and discards to the
  bottom of the next. Draw and discard are one atomic action.
- A player never discards their preferred denomination (player i prefers i).
  The discard is chosen at random from the other cards.

## Testing

```bash
mvn clean test
```
Expected result: `BUILD SUCCESS` with all tests passing. Reports are written
to `target/surefire-reports/`. 
To run one class:
```bash
mvn test -Dtest=GameIntegrationTest
```

Notes:
- Tests use JUnit 5.
- Full-game tests have a 10 second timeout; a timeout failure means the game
  did not terminate.
- Concurrency tests can reveal things like deadlocks but not ensure the game runs perfectly every run, tests can be run several times for more security.

What is covered:
- **Card**: non-negative values only, immutability.
- **Deck**: FIFO order, empty-deck behaviour, output format, concurrent adds and draws.
- **Player**: preferred denominations never discarded, hands stay at four
  cards, immediate-win detection, output-file format, stopping after another
  player wins.
- **Setup**: round-robin dealing and deck filling, ring wrap-around.
- **Input handling**: invalid player counts and invalid packs, with re-prompting.
- **Concurrency**: a four-player ring with no deadlock and card conservation.
- **End to end**: a full game ends with exactly one winner, 2n output files, and
  every card in the hands and decks accounted for.

### Without Maven
Download the JUnit console launcher
`junit-platform-console-standalone-1.11.4.jar` from Maven Central into `lib/`,
then:
```bash
mkdir -p out
javac -cp lib/junit-platform-console-standalone-1.11.4.jar -d out \
      src/main/java/com/example/*.java src/test/java/com/example/*.java
java -jar lib/junit-platform-console-standalone-1.11.4.jar \
      --class-path out --scan-class-path
```
On Windows, use `\` in the source paths.

## Project structure

```
pom.xml
README.md
sample-pack.txt
src/main/java/com/example/    CardGame, Player, Deck, Card
src/test/java/com/example/    JUnit 5 tests
target/                       compiled classes and cards.jar
```

`CardGame`: reads input, deals the pack, starts and joins the player threads, writes deck files (entry point to program)
`Player`: Runnable player: atomic draw/discard, discard strategy, win detection, output log
`Deck`: Thread-safe FIFO queue of cards; writes its own output file
`Card`: Immutable card with a non-negative value

## Known limitations
- A player facing an empty deck retries until a card arrives rather than blocking.
- If two players reach a winning hand at the same moment, the first to claim the
  win is recorded.
- The discard strategy is random, so a game is not guaranteed to finish quickly

## License

Submitted for assessment at the University of Exeter. All rights reserved by
the authors; not licensed for reuse.