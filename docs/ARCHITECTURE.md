# Sudoku Discord Bot Architecture

## Overview

This document defines the technical architecture for the Sudoku Discord project.

The product is split into two user-facing pieces:

- a Discord bot for slash commands and session launch
- a Discord Activity for the actual gameplay UI

The backend is the source of truth for all game state and rule evaluation. The frontend is intentionally thin and only renders backend-provided state plus local UI interaction state.

## Tech Stack

### Frontend

- React
- TypeScript
- Vite
- Discord Embedded App SDK

### Backend

- Java
- Spring Boot
- JDA

### Data

- PostgreSQL

## Architecture Principles

- Backend owns all game logic
- Backend owns validation, hint evaluation, puzzle selection, session state transitions, and persistence
- Frontend does not perform authoritative Sudoku validation
- Frontend sends player actions and renders the state returned by the backend
- The database persists puzzles, sessions, players, and game progress

## System Responsibilities

### Discord Bot Layer

The bot is responsible for:

- registering and handling slash commands
- accepting `/sudoku mode:<single|collab> difficulty:<easy|medium|hard|lethal>`
- creating or requesting a new game session from the backend
- posting lightweight channel status messages
- launching the Discord Activity

The bot should remain thin. It should not duplicate game rules that already exist in the backend service.

### Backend Service

The backend is responsible for:

- selecting puzzles from the puzzle dataset
- creating game sessions
- validating moves
- applying guided or classic validation rules
- handling hints
- updating board state
- saving progress
- loading saved sessions
- determining game completion and lifecycle state
- supporting future collaboration rules and concurrency control

The backend is the source of truth for all persistent and shared state.

### Frontend Activity

The Activity frontend is responsible for:

- rendering the Sudoku board
- handling cell selection
- handling number input, pencil mode, erase, and hint actions
- displaying temporary feedback such as `Spot on` or `Incorrect`
- reflecting the state returned by the backend

The frontend should not decide whether a move is correct. It should only present the result returned by the backend.

## MVP1 Request Flow

### Start Game

```text
User runs /sudoku mode:single difficulty:<level>
-> Discord sends interaction to bot
-> bot asks backend to create a new game session
-> backend selects a puzzle and stores the session
-> bot posts a lightweight channel status message
-> bot launches the Discord Activity
-> Activity loads the created session from backend
```

### Play Turn

```text
User selects a cell and enters a value
-> frontend sends action to backend
-> backend validates and applies the action
-> backend persists the updated state
-> backend returns the updated game state and feedback metadata
-> frontend re-renders the board
```

### Resume Game

```text
User returns to an existing active game
-> frontend requests the saved session from backend
-> backend returns current persisted state
-> frontend renders the saved board
```

## MVP1 Difficulty Model

- Difficulty is chosen from the slash command
- Difficulty selection is not handled in the Activity UI for MVP1
- The Activity starts with the session already configured
- A future version may move difficulty selection into the Activity pre-game flow

## Validation Model

The long-term design supports two validation styles:

- guided
- classic

For MVP1:

- guided is the default and primary supported experience
- backend validates entries against the solved board
- frontend renders temporary success or failure feedback based on backend response

## State Ownership

### Backend-Owned State

- session id
- puzzle id
- difficulty
- solved board
- original clue layout
- current entered values
- pencil marks
- game status
- hint usage
- elapsed timing metadata
- player participation metadata

### Frontend-Owned State

- current local selection
- temporary animation state
- temporary visibility state for feedback banners or labels
- non-authoritative presentation state

Frontend state must always be disposable and reconstructable from backend state plus current user interaction.

## API Contract Shape

The first concrete API contract now lives in [`API.md`](D:\Projects\sudo\docs\API.md).

The responsibility split is:

### Frontend Sends

- session id
- player id or Discord user context
- action type
- action payload

Example action types:

- select cell
- enter value
- toggle pencil mode
- erase value
- request hint
- reset game
- cancel game

### Backend Returns

- session metadata
- board state
- clue cells
- user-entered cells
- pencil marks
- current game status
- validation result
- temporary feedback metadata
- timing/progress metadata

## Persistence Model

PostgreSQL stores at least:

- puzzle catalog
- puzzle difficulty
- solved puzzle data
- game sessions
- current board progress
- pencil marks
- participant records
- lifecycle status
- timestamps

The persistence design must support:

- bot restarts
- resume flow
- future collaborative sessions

## Collaboration Readiness

Collaboration is not in MVP1, but the architecture should allow it later without reworking the entire stack.

Design implications:

- game state should already be modeled as a session, not a single-player-only object
- backend should own authoritative updates
- frontend should already assume the state can change outside local input
- concurrency control belongs in the backend

Planned MVP2 collaboration behavior:

- collab sessions started from the same `/sudoku` command
- players join from a public channel message
- backend manages participant membership
- shared board state is synchronized from the backend
- first write wins for conflicting cell updates

## Suggested Repository Layout

```text
/
|-- ui
|-- backend
|-- docs
|-- README.md
|   |-- API.md
|   |-- ARCHITECTURE.md
|   |-- DESIGN.md
|   |-- DOMAIN.md
|   `-- SCHEMA.md
`-- pom.xml
```

Suggested contents:

- `ui`: React Activity app
- `backend`: Spring Boot service and JDA bot integration
- `docs`: future technical notes, UX references, schemas, and planning docs

## Open Questions

- Exact Activity launch flow from the bot
- Whether live session updates need polling or push transport in MVP1
- Final DB schema
- How production authentication and Discord identity are passed from Activity to backend
