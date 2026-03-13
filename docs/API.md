# Sudoku Discord Bot API Contract

## Overview

This document defines the first backend API contract for MVP1.

It is designed for:

- Discord bot session creation
- Discord Activity session loading
- solo gameplay in MVP1
- future collaborative extension without breaking core response shapes

The backend remains the authoritative owner of all game logic and state.

## API Style

- transport: HTTPS + JSON
- base path: `/api/v1`
- time format: ISO 8601 UTC timestamps
- ids: UUID strings for internal entities
- Discord ids: strings

## MVP1 Scope

The API supports:

- create a game session
- fetch current game state
- submit a final value
- update pencil marks
- erase a cell value
- request a hint
- reset a game
- cancel a game

The API does not yet require:

- push updates
- collaborative presence
- lobby management
- host transfer endpoints

## Core Design Decisions

### Validation Mode

- MVP1 defaults to `guided`
- `classic` remains part of the domain model and API enum surface
- the client should treat `validationMode` as backend-owned session metadata

### Resource Shape

Two response families are enough for MVP1:

- `GameSessionState` for full state reads
- `GameActionResult` for mutations that return the updated state plus action feedback

This keeps the frontend simple and avoids a separate patch format in the first version.

## Authentication Context

Exact production authentication is still TBD, but every mutating request must carry the acting Discord user identity.

Suggested interim header:

- `X-Discord-User-Id`

Later this can be replaced by signed Discord Activity or bot-authenticated identity propagation without changing the request bodies.

## Enumerations

### Difficulty

- `easy`
- `medium`
- `hard`
- `lethal`

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

### CellFeedbackType

- `correct`
- `incorrect`

### ActionType

- `enter_value`
- `set_pencil_marks`
- `erase_cell`
- `request_hint`
- `reset_game`
- `cancel_game`

## Data Contracts

### GameSessionState

```json
{
  "gameId": "1e7e8f61-2f62-4af1-9c38-1c3ea66d2d50",
  "puzzleId": "c7fa5f3f-59f3-496a-925b-c10da6e92033",
  "guildId": "123456789012345678",
  "channelId": "234567890123456789",
  "mode": "single",
  "status": "active",
  "difficulty": "medium",
  "validationMode": "guided",
  "hostUserId": "345678901234567890",
  "createdByUserId": "345678901234567890",
  "startedAt": "2026-03-13T11:30:00Z",
  "updatedAt": "2026-03-13T11:31:20Z",
  "completedAt": null,
  "board": {
    "cells": [
      {
        "row": 0,
        "column": 0,
        "isClue": true,
        "clueValue": 5,
        "enteredValue": null,
        "effectiveValue": 5,
        "pencilMarks": [],
        "lastUpdatedBy": null,
        "lastUpdatedAt": null,
        "lastFeedback": null
      },
      {
        "row": 0,
        "column": 1,
        "isClue": false,
        "clueValue": null,
        "enteredValue": 7,
        "effectiveValue": 7,
        "pencilMarks": [],
        "lastUpdatedBy": "345678901234567890",
        "lastUpdatedAt": "2026-03-13T11:31:20Z",
        "lastFeedback": {
          "type": "correct",
          "message": "Spot on",
          "expiresAt": "2026-03-13T11:31:24Z"
        }
      }
    ]
  },
  "progress": {
    "filledEditableCells": 12,
    "totalEditableCells": 51,
    "mistakeCount": 2,
    "hintCount": 1,
    "completionPercent": 23
  },
  "players": [
    {
      "userId": "345678901234567890",
      "role": "host",
      "membershipStatus": "active",
      "joinedAt": "2026-03-13T11:30:00Z",
      "lastSeenAt": "2026-03-13T11:31:20Z"
    }
  ]
}
```

Notes:

- `cells` always contains 81 entries
- `effectiveValue` is `clueValue` for clue cells, otherwise `enteredValue`
- `lastFeedback` is optional transient metadata for rendering guided-mode feedback
- `mistakeCount` is an accumulated session metric, not cell state

### GameActionResult

```json
{
  "actionId": "8f5af67a-fdd9-4f41-a6f0-8bdb8d4c77f9",
  "actionType": "enter_value",
  "accepted": true,
  "rejectionReason": null,
  "feedback": {
    "type": "correct",
    "message": "Spot on",
    "row": 0,
    "column": 1,
    "expiresAt": "2026-03-13T11:31:24Z"
  },
  "state": {}
}
```

Notes:

- `state` is a full `GameSessionState`
- `accepted = false` is used for rejected requests such as editing a clue cell or violating classic-mode conflict rules
- `feedback` may be null for non-feedback actions such as reset or cancel

### Error Response

```json
{
  "error": {
    "code": "GAME_NOT_FOUND",
    "message": "No game exists for the provided id.",
    "details": null
  }
}
```

Suggested error codes:

- `GAME_NOT_FOUND`
- `PUZZLE_NOT_FOUND`
- `UNAUTHORIZED_PLAYER`
- `ACTIVE_GAME_CONFLICT`
- `INVALID_ACTION`
- `INVALID_CELL`
- `CLUE_CELL_EDIT_FORBIDDEN`
- `GAME_NOT_ACTIVE`
- `UNSUPPORTED_MODE`

## Endpoints

### POST `/game`

Creates a new game session.

Primary caller:

- Discord bot slash-command handler

Request:

```json
{
  "mode": "single",
  "difficulty": "medium",
  "validationMode": "guided",
  "guildId": "123456789012345678",
  "channelId": "234567890123456789",
  "createdByUserId": "345678901234567890"
}
```

Response:

- `201 Created`
- body: `GameSessionState`

Rules:

- reject if the user already has another active game
- `validationMode` may be omitted by the caller and defaulted to `guided`
- MVP1 should reject `mode = collab` with `UNSUPPORTED_MODE` until collaboration is implemented

### GET `/game/{gameId}`

Returns the latest persisted session state.

Primary callers:

- Discord Activity initial load
- resume flow

Response:

- `200 OK`
- body: `GameSessionState`

### POST `/game/{gameId}/actions/enter-value`

Places or replaces the final value in one editable cell.

Request:

```json
{
  "row": 0,
  "column": 1,
  "value": 7
}
```

Response:

- `200 OK`
- body: `GameActionResult`

Rules:

- reject edits to clue cells
- clear pencil marks for the same cell if the action is accepted
- in `guided`, keep the value and return correctness feedback
- in `classic`, reject conflicting moves immediately

### PUT `/game/{gameId}/cells/{row}/{column}/pencil-marks`

Replaces the full pencil-mark set for one editable cell.

Request:

```json
{
  "marks": [1, 3, 8]
}
```

Response:

- `200 OK`
- body: `GameActionResult`

Rules:

- marks must be unique digits from `1` to `9`
- setting `marks: []` clears the pencil marks
- clue cells cannot receive pencil marks

### POST `/game/{gameId}/actions/erase-cell`

Clears the entered value for one editable cell.

Request:

```json
{
  "row": 0,
  "column": 1
}
```

Response:

- `200 OK`
- body: `GameActionResult`

Rules:

- erasing a clue cell is rejected
- pencil marks are left unchanged unless product rules later decide otherwise

### POST `/game/{gameId}/actions/request-hint`

Reveals the correct value for one editable cell.

Request:

```json
{
  "row": 0,
  "column": 1
}
```

Response:

- `200 OK`
- body: `GameActionResult`

Rules:

- hint must target an editable cell
- accepted hint writes the correct final value into the cell
- accepted hint clears pencil marks in that cell
- backend increments session hint usage metrics

### POST `/game/{gameId}/actions/reset`

Resets the board to the original puzzle state.

Request:

```json
{}
```

Response:

- `200 OK`
- body: `GameActionResult`

Rules:

- clears all non-clue entered values
- clears all pencil marks
- preserves the same `gameId`
- preserves `startedAt`
- updates `updatedAt`
- resets completion state if the game had not been cancelled

### POST `/game/{gameId}/actions/cancel`

Cancels the current game.

Request:

```json
{}
```

Response:

- `200 OK`
- body: `GameActionResult`

Rules:

- only the game owner should be able to cancel in MVP1
- cancelled games become read-only

## State Transition Rules

### Game Creation

- created session starts in `active` for solo MVP1
- `pending` remains reserved for future collaborative lobby flow

### Completion

- backend marks the session `completed` when all editable cells match the solved board
- `completedAt` is set once and never cleared

### Cancellation

- backend marks the session `cancelled`
- further mutating actions return `GAME_NOT_ACTIVE`

## Frontend Integration Notes

The Discord Activity should:

- fetch `GET /game/{gameId}` on load
- treat the returned state as the single source of truth
- optimistically update only local selection state, not authoritative board state
- re-render from the returned `state` after every mutation

The bot should:

- call `POST /game` after the slash command
- receive `gameId`
- use that id to launch or deep-link the Activity session

## Deferred Endpoints

These are intentionally not part of MVP1:

- `POST /game/{gameId}/join`
- `POST /game/{gameId}/leave`
- `POST /game/{gameId}/transfer-host`
- `GET /game/{gameId}/events`
- WebSocket or SSE sync endpoints

## Open Questions

- whether reset should also reset accumulated `mistakeCount` and `hintCount`
- whether solo cancel should remain host-only or simply actor-only
- whether `GET /game/{gameId}` also needs ETag/version metadata for later concurrency control
- whether the backend should expose a lightweight list endpoint for "resume my active game"
