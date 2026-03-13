# Sudoku Backend API

## Status

This document describes the API that is implemented in the backend today.

Currently implemented:

- create a game session
- validation and error responses for that endpoint

Not implemented yet:

- fetch game session by id
- board/action endpoints
- hint/reset/cancel endpoints

## Base Path

- Base path: `/api/v1`
- Transport: JSON over HTTP
- Time format: ISO 8601 UTC timestamps
- Enum values are serialized in uppercase because the backend currently uses Java enum names directly

## Implemented Enumerations

### Difficulty

- `EASY`
- `MEDIUM`
- `HARD`
- `LETHAL`

### GameMode

- `SINGLE`
- `COLLAB`

### ValidationMode

- `GUIDED`
- `CLASSIC`

### GameStatus

- `PENDING`
- `ACTIVE`
- `COMPLETED`
- `CANCELLED`
- `ABANDONED`

## Implemented Resource Shapes

### CreateGameRequest

```json
{
  "mode": "SINGLE",
  "difficulty": "MEDIUM",
  "validationMode": "GUIDED",
  "guildId": "123456789012345678",
  "channelId": "234567890123456789",
  "hostUserId": "345678901234567890",
  "createdByUserId": "345678901234567890"
}
```

Notes:

- `validationMode` is currently required by the backend
- `hostUserId` is currently required by the backend
- `createdByUserId` is currently required by the backend
- collaborative mode is not supported yet even though `COLLAB` exists in the enum surface

### GameSession

Current create response body:

```json
{
  "gameId": "1e7e8f61-2f62-4af1-9c38-1c3ea66d2d50",
  "puzzleId": "c7fa5f3f-59f3-496a-925b-c10da6e92033",
  "guildId": "123456789012345678",
  "channelId": "234567890123456789",
  "status": "ACTIVE",
  "mode": "SINGLE",
  "validationMode": "GUIDED",
  "difficulty": "MEDIUM",
  "hostUserId": "345678901234567890",
  "createdByUserId": "345678901234567890",
  "startedAt": "2026-03-13T11:30:00Z",
  "updatedAt": "2026-03-13T11:30:00Z",
  "completedAt": null
}
```

Notes:

- the current response is a flat `GameSession` object
- board state, progress, and player lists are not part of the implemented response yet

### Error Response

Current error shape from `GlobalControllerAdvice`:

```json
{
  "timestamp": "2026-03-13T11:31:20Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "guildId must not be blank",
  "path": "/api/v1/game/create"
}
```

Possible current error codes include:

- `VALIDATION_ERROR`
- `BAD_REQUEST`
- `INTERNAL_SERVER_ERROR`
- `NO_ELIGIBLE_PUZZLE_AVAILABLE`
- `GAME_NOT_FOUND`
- `ACTIVE_GAME_ALREADY_EXISTS`
- `UNSUPPORTED_MODE`
- `INVALID_ACTION`
- `INVALID_CELL`
- `INVALID_VALUE`
- `CLUE_CELL_EDIT_FORBIDDEN`
- `FORBIDDEN_ACTION`

## Implemented Endpoints

### POST `/game/create`

Creates a new game session.

Full path:

- `/api/v1/game/create`

Request body:

- `CreateGameRequest`

Success response:

- `201 Created`
- body: `GameSession`

Current behavior:

- only `SINGLE` mode is supported
- `COLLAB` currently returns `UNSUPPORTED_MODE`
- invalid request bodies return `400`
- if no eligible puzzle is available, the backend returns a handled error response

## Known Contract Gaps

These are intentionally not documented as implemented because the backend does not expose them yet:

- `GET /api/v1/game/{gameId}`
- `POST /api/v1/game/{gameId}/actions/enter-value`
- `PUT /api/v1/game/{gameId}/cells/{row}/{column}/pencil-marks`
- `POST /api/v1/game/{gameId}/actions/erase-cell`
- `POST /api/v1/game/{gameId}/actions/request-hint`
- `POST /api/v1/game/{gameId}/actions/reset`
- `POST /api/v1/game/{gameId}/actions/cancel`

## Next Recommended Contract Changes

If you want the docs and implementation to converge toward the planned product API, the next backend changes should be:

1. Change create from `/api/v1/game/create` to `/api/v1/game` or document `/create` as a deliberate choice long-term.
2. Decide whether enum values should stay uppercase or be normalized to lowercase externally.
3. Decide whether `validationMode` should remain required or default to `GUIDED`.
4. Add `GET /api/v1/game/{gameId}` before documenting board/action endpoints as active API surface.
