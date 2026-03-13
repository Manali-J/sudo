# sudo

`sudo` is a Sudoku Discord bot project focused on multiplayer-friendly puzzle play, including a planned collaborative mode where multiple users can work on the same board inside Discord.

## Status

This repository is still at the scaffold stage. The Maven project exists under [`sudoku/`](D:\Projects\sudo\sudoku), but the Discord bot, Sudoku engine, and collaboration flow are not implemented yet.

## Project Goals

- Run Sudoku games directly from Discord.
- Support solo and collaborative game modes.
- Let multiple users contribute moves to the same puzzle.
- Track game state, mistakes, and puzzle completion in-channel.
- Keep the bot structure clean enough to extend with commands, persistence, and difficulty settings.

## Planned Collaborative Mode

The main feature for this bot is shared Sudoku solving. The intended experience is:

- A user starts a collaborative puzzle in a Discord channel.
- Other users join the same active board.
- Players submit moves through bot commands or interactions.
- The bot validates moves and updates the shared state.
- The channel shows progress until the puzzle is solved or cancelled.

Possible future additions:

- Turn-free collaboration or optional turn-based mode
- Per-user contribution stats
- Hint system
- Timed games and leaderboards
- Puzzle save/resume support

## Tech Stack

- Java
- Maven
- Discord bot integration to be added

## Repository Layout

```text
.
|-- backend
|   |-- pom.xml
|   `-- src
|-- docs
|   |-- API.md
|   |-- ARCHITECTURE.md
|   |-- DESIGN.md
|   |-- DOMAIN.md
|   `-- SCHEMA.md
|-- pom.xml
|-- README.md
```

## Getting Started

### Prerequisites

- Java 25, based on the current Maven compiler configuration in [`pom.xml`](D:\Projects\sudo\pom.xml)
- Maven

### Build

From the project root:

```powershell
mvn -pl backend compile
```

## Next Steps

- Add a real backend application entrypoint in [`Main.java`](D:\Projects\sudo\backend\src\main\java\org\game\Main.java)
- Choose a Discord Java library such as JDA
- Implement Sudoku board generation and validation
- Design command flow for solo and collaborative sessions
- Add tests for puzzle rules and game state transitions

## Documentation

- Product design: [`docs/DESIGN.md`](D:\Projects\sudo\docs\DESIGN.md)
- System architecture: [`docs/ARCHITECTURE.md`](D:\Projects\sudo\docs\ARCHITECTURE.md)
- Domain model: [`docs/DOMAIN.md`](D:\Projects\sudo\docs\DOMAIN.md)
- Database schema: [`docs/SCHEMA.md`](D:\Projects\sudo\docs\SCHEMA.md)
- API contract: [`docs/API.md`](D:\Projects\sudo\docs\API.md)

## Notes

The current source tree appears to be an IDE-generated starter project. Expect this README to evolve alongside the actual bot implementation.
