# Sudoku Discord Bot Domain Model

## Overview

This document defines the core domain model for the Sudoku project. It sits between the product design in [`DESIGN.md`](D:\Projects\sudo\docs\DESIGN.md) and the storage design in [`SCHEMA.md`](D:\Projects\sudo\docs\SCHEMA.md).

The domain model is designed for MVP1 solo play, while keeping collaborative play possible without redesigning the core entities.

## Design Principles

- The backend is the authoritative owner of all domain state
- Domain entities should be collaboration-ready even if MVP1 is solo-first
- Puzzle data should be reusable across many game sessions
- Session state should be reconstructable from persisted state

## Core Concepts

### Puzzle

A `Puzzle` is a reusable Sudoku definition from the puzzle catalog.

Responsibilities:

- stores the initial clue layout
- stores the solved board
- stores difficulty classification
- may store metadata about source/import origin

Key fields:

- `puzzleId`
- `difficulty`
- `clueBoard`
- `solutionBoard`
- `source`
- `createdAt`

Notes:

- One puzzle can back many game sessions
- The clue board and solution board should be immutable once stored

### GameSession

A `GameSession` is a running or completed instance of a puzzle being played by one or more users.

Responsibilities:

- ties a puzzle to a live play session
- tracks current state of the board
- tracks current game lifecycle status
- tracks mode and validation style
- tracks host and participation state

Key fields:

- `gameId`
- `puzzleId`
- `guildId`
- `channelId`
- `status`
- `mode`
- `validationMode`
- `difficulty`
- `hostUserId`
- `createdByUserId`
- `startedAt`
- `updatedAt`
- `completedAt`

Notes:

- In MVP1, `mode` is effectively `single`
- In MVP2, the same entity supports `collab`

### PlayerSession

A `PlayerSession` represents a user's participation in a game session.

Responsibilities:

- tracks which users belong to a game
- tracks whether they are currently active, left, removed, or rejoined
- supports future collaborative membership logic

Key fields:

- `playerSessionId`
- `gameId`
- `userId`
- `role`
- `membershipStatus`
- `joinedAt`
- `leftAt`
- `lastSeenAt`

Notes:

- In solo mode there is still one `PlayerSession`
- In collaborative mode there may be many `PlayerSession` records for one `GameSession`

### CellState

A `CellState` represents the current mutable state of a board cell inside a game session.

Responsibilities:

- tracks final entered value for the cell
- distinguishes clue cells from user-editable cells
- stores who last changed the cell
- stores recent correctness feedback information

Key fields:

- `gameId`
- `rowIndex`
- `columnIndex`
- `clueValue`
- `enteredValue`
- `isClue`
- `lastUpdatedBy`
- `lastUpdatedAt`
- `lastFeedbackType`
- `lastFeedbackAt`

Notes:

- There are exactly 81 logical cells per game session
- Clue cells are immutable
- `enteredValue` may be correct or incorrect depending on validation mode
- `isClue = true` means the cell is part of the original puzzle and is not editable
- `isClue = true` should always imply `clueValue` is set to a value from `1-9`
- `isClue = false` should imply `clueValue` is `null`

### PencilMarks

`PencilMarks` represent candidate notes for a specific editable cell in a specific session.

Responsibilities:

- stores note digits for a cell
- supports add/remove behavior in pencil mode
- clears automatically when a final number is placed

Key fields:

- `gameId`
- `rowIndex`
- `columnIndex`
- `markSet`
- `updatedBy`
- `updatedAt`

Notes:

- `markSet` can be modeled as a compact set of digits `1-9`
- Pencil marks belong to the shared board state in the current design

### HintUsage

A `HintUsage` record tracks hint actions applied during a game.

Responsibilities:

- records that a hint was used
- records which cell was revealed
- supports future analytics or scoring changes

Key fields:

- `hintId`
- `gameId`
- `usedBy`
- `rowIndex`
- `columnIndex`
- `hintType`
- `revealedValue`
- `usedAt`

Notes:

- MVP1 only needs `reveal_cell`
- Future hint types can be added without changing the rest of the domain

### FeedbackEvent

A `FeedbackEvent` represents temporary guided-mode UI feedback for a recent action.

Responsibilities:

- records whether the latest action was correct or incorrect
- provides data for temporary frontend rendering
- supports expiration of feedback highlights

Key fields:

- `feedbackId`
- `gameId`
- `userId`
- `rowIndex`
- `columnIndex`
- `feedbackType`
- `message`
- `createdAt`
- `expiresAt`

Notes:

- This is transient by nature, but may still be persisted if you want resumable short-lived UI feedback
- MVP1 can also choose to return this only in the API response and not store it long-term

## Domain Enumerations

### GameMode

- `single`
- `collab`

### ValidationMode

- `guided`
- `classic`

### GameStatus

- `pending`
- `active`
- `completed`
- `cancelled`
- `abandoned`

### PlayerRole

- `host`
- `participant`

### MembershipStatus

- `active`
- `left`
- `removed`

### HintType

- `reveal_cell`

### FeedbackType

- `correct`
- `incorrect`

## Aggregate Boundaries

Recommended aggregate ownership:

### Puzzle Aggregate

- `Puzzle`

### Game Aggregate

- `GameSession`
- `PlayerSession`
- `CellState`
- `PencilMarks`
- `HintUsage`

The `GameSession` should be treated as the main aggregate root for runtime game operations.

## Important Domain Rules

- A user may only belong to one active game at a time across the bot
- A clue cell is never editable
- Entering a final value replaces an existing user-entered value
- Entering a final value clears pencil marks in that same cell
- In guided mode, correctness is checked against the solved board immediately
- In classic mode, only row/column/box conflicts are rejected immediately
- If the host leaves a collaborative game, host transfers to the earliest joined remaining active participant

## MVP1 Scope Mapping

The following entities are required immediately for MVP1:

- `Puzzle`
- `GameSession`
- `PlayerSession`
- `CellState`
- `PencilMarks`
- `HintUsage`

The following can remain lightweight or optional in MVP1:

- `FeedbackEvent`

## Future Extensions

- player scoring and leaderboard entities
- activity telemetry
- collaborative presence overlays
- puzzle import batches and moderation metadata
