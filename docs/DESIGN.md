# Sudoku Discord Bot Design

## Overview

This document defines the product and interaction design for the Sudoku Discord bot and companion Discord Activity. The current focus is MVP1, which supports solo play. Collaborative play is planned for MVP2 and is included here only where it affects present-day design decisions.

## Product Goals

- Let users play Sudoku entirely inside Discord.
- Use a Discord Activity for the actual play surface.
- Keep the command surface small and push gameplay into the game UI.
- Make solo play private to the player while still allowing a lightweight public status message.
- Design the core session and game state model so collaboration can be added later without restructuring the bot.

## MVP Scope

### MVP1

- Solo Sudoku gameplay
- Slash-command game creation
- Difficulty selection
- Discord Activity-based board UI
- Guided validation against the solved board with temporary correctness feedback
- Pencil marks
- Hint support with reveal-cell behavior
- Persistent storage in PostgreSQL
- Resume support after bot restart

### MVP2

- Collaborative play
- Join flow from a public channel message
- Shared board state for joined players
- Host transfer and player management

### MVP3

- Rewards, points, and leaderboard support

## Platform Model

The product uses two Discord-facing pieces:

- A bot layer for slash commands, status messages, and session launch flow
- A Discord Activity for the actual Sudoku game UI

Rationale:

- The Activity supports a true game-like interface with a clickable board
- It avoids Discord message component limits for a 9x9 grid
- It is a better fit for solo play now and collaborative play later

## Core Command

MVP1 uses a single primary slash command:

```text
/sudoku mode:<single|collab> difficulty:<easy|medium|hard|lethal>
```

Notes:

- `single` is part of MVP1.
- `collab` is planned for MVP2, but the command shape is defined now so it does not need to change later.
- Session actions such as leave, resume, cancel, reset, join, hint, and player removal are handled through the game UI rather than separate slash commands.

## Game Modes

### Solo

- Included in MVP1
- Board is shown inside the Discord Activity for the player
- Channel shows only a lightweight status message
- No `Join Game` button is shown

### Collaborative

- Planned for MVP2
- Freeform collaboration, not turn-based
- Players join from a public `Join Game` button in the channel
- The public message shows the player list and game status
- Joined players interact with the shared board state through their private game view
- New players may join at any time while the game is active

### Open vs Private Collaboration

- Open collaboration means any eligible user who can see the public game message can join
- Private collaboration means only explicitly allowed users can join
- Private collaboration is future work

## Session Model

- A new game is created for each `/sudoku` command
- Multiple games can exist in the same guild
- A user may only participate in one active game at a time across the bot
- A player may leave and later rejoin the same game they were already part of

## Roles and Permissions

### Player

- Can place values
- Can erase values
- Can use hints
- Can manage pencil marks

### Host

- The user who started the collaborative game
- Can cancel the game
- Can reset the game
- Can lock or unlock collaboration
- Can remove a player from the game

### Host Transfer

- If the host leaves a collaborative game, host ownership automatically transfers to the earliest joined remaining participant

### Server Admin

- Can override host restrictions

## Game Lifecycle

Recommended game statuses:

- `pending`
- `active`
- `completed`
- `cancelled`
- `abandoned`

Definitions:

- `pending`: the session exists but is not fully in progress yet; mainly useful for collaborative lobby flow
- `active`: the game is currently in progress
- `completed`: the puzzle has been solved
- `cancelled`: the game was ended manually
- `abandoned`: the game is no longer being continued; timeout policy is still TBD

## Validation and Rules

The game supports two validation styles:

- `guided`
- `classic`

### Guided Mode

- Entered values are checked immediately against the solved board
- Correct values remain in the cell
- Incorrect values also remain in the cell until the user changes or erases them
- Correctness feedback is shown visually for a short duration

### Classic Mode

- Moves that clash with existing values in the same row, column, or 3x3 box are rejected immediately
- Non-clashing but ultimately incorrect guesses are allowed
- This keeps gameplay closer to traditional Sudoku play

### MVP1 Decision

- MVP1 defaults to guided play
- guided mode checks entries against the solved board and returns temporary success or failure feedback
- Classic mode is part of the long-term design, but it does not need to be exposed in the first version

### Shared Rules

- Original puzzle clue cells are not editable

## Hints

MVP1 hint behavior:

- Reveal the correct value for the selected editable cell

Out of scope for MVP1:

- Soft hints such as "check this row"
- Identifying an incorrect filled cell

## Puzzle and Difficulty Model

- Difficulty is user-facing in MVP1
- Supported difficulty levels:
  - `easy`
  - `medium`
  - `hard`
  - `lethal`
- Puzzles should come from a stored dataset in PostgreSQL
- Dynamic puzzle generation is optional future work

The persistent model should support at least:

- `gameId`
- `puzzleId`
- `difficulty`
- initial puzzle/clue state
- solved board
- current entered values
- pencil marks
- participants
- host
- lock status
- timestamps
- game status

## Solo MVP1 UX

### Public Channel Message

When a player starts a solo game, the bot posts a lightweight status message in the channel.

Suggested content:

- player name
- difficulty
- current status

The full board is not shown publicly.

### Private Game View

The Discord Activity view is the main play surface.

It should contain:

- difficulty
- elapsed time
- game status
- Sudoku board
- current selected cell indicator
- tool controls
- number input controls

### Board Rendering

The board should be rendered directly inside the Discord Activity rather than trying to make the full 9x9 grid out of Discord message buttons.

Reasons:

- better visual quality
- easier highlighting of row, column, box, and matching numbers
- room for pencil marks
- supports direct cell interaction
- avoids Discord message component layout limits

The board should visually distinguish:

- original clue cells
- user-entered values
- pencil marks
- selected cell
- highlighted row
- highlighted column
- highlighted 3x3 box
- matching values already present on the board

Pencil marks should render as smaller numbers inside the cell.

## Solo Interaction Model

Cell interaction is the primary interaction pattern.

When a cell is selected:

- the selected cell is highlighted
- the corresponding row is highlighted
- the corresponding column is highlighted
- the corresponding 3x3 box is highlighted
- matching numbers already present on the board may also be highlighted

After selecting a cell, the player can:

- enter a number
- add or remove pencil marks
- erase the value
- request a hint

## Solo Controls

Recommended MVP1 control layout:

### Board Interaction

- Players interact directly with cells on the board
- The selected cell becomes the current target for input

### Tool Controls

- `Pencil` toggle
- `Erase`
- `Hint`
- `Reset`
- `Cancel`
- `Resume`

### Number Controls

- buttons `1` through `9`

## Input Feedback

Guided mode uses temporary correctness feedback.

Correct entry:

- the entered value stays in the cell
- the cell gets a green border or highlight
- a short success message such as `Spot on` is shown
- the temporary feedback fades after roughly 3 to 5 seconds

Incorrect entry:

- the entered value stays in the cell
- the cell gets a red border or highlight
- a short error message such as `Incorrect` is shown
- the temporary feedback fades after roughly 3 to 5 seconds

Temporary feedback should not remain permanently on the board. The value remains, but the green or red styling disappears after the feedback window ends.

## Cell Edit Behavior

- If a player selects an editable cell and enters a number, that number replaces the existing user-entered value immediately
- `Erase` clears the selected editable cell entirely
- Original clue cells are not editable
- If a final number is placed in a cell, all pencil marks in that cell are cleared automatically

## Persistence

- PostgreSQL is the intended database
- Games should survive bot restarts
- Save and resume are required

This design intentionally treats persistence as a core feature rather than a later add-on.

## Collaboration Notes for MVP2

These are not in scope for MVP1, but the current Activity-based solo design should not block them.

- Collaborative game starts from the same `/sudoku` command with `mode:collab`
- Public channel message includes a `Join Game` button and player list
- Joined players see the same shared board state
- Each player may also see temporary selection or presence overlays for their own interaction state
- First write wins when two users act on the same cell at nearly the same time
- Compare-and-swap style updates should be used to avoid stale overwrites
- A per-user visual color may be used later for temporary presence indication, avoiding red, green, amber, and gray

## Open Questions

- Exact Activity layout for the board controls
- Final visual design for pencil marks, highlights, and temporary feedback styling
- Timeout policy for when a game becomes `abandoned`
- Whether `lethal` has any special behavior beyond being the hardest difficulty label
