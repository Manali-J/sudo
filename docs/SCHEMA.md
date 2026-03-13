# Sudoku Discord Bot Database Schema

## Overview

This document proposes the initial PostgreSQL schema for the Sudoku project. It maps the domain described in [`DOMAIN.md`](D:\Projects\sudo\docs\DOMAIN.md) into a storage model that supports MVP1 solo play and future collaborative play.

This is not a migration file. It is the design reference for the first database version.

## Schema Goals

- store reusable puzzle definitions
- persist active and completed game sessions
- support resume after restart
- support pencil marks and hints
- support future collaborative participation without redesigning core tables

## Table Overview

Recommended initial tables:

- `puzzles`
- `game_sessions`
- `game_players`
- `game_cells`
- `game_pencil_marks`
- `game_hints`

Optional later tables:

- `game_feedback_events`
- `leaderboard_entries`
- `puzzle_import_batches`

## 1. puzzles

Stores reusable Sudoku puzzles.

Suggested columns:

- `id` UUID primary key
- `difficulty` VARCHAR not null
- `clue_board` TEXT not null
- `solution_board` TEXT not null
- `source` VARCHAR null
- `created_at` TIMESTAMP not null

Notes:

- `clue_board` and `solution_board` can be stored as compact 81-character strings
- Example encoding:
  - clue board: `530070000...`
  - solution board: `534678912...`

Recommended constraints:

- unique constraint on `(clue_board, solution_board)`
- check that `difficulty` is one of the supported values

## 2. game_sessions

Stores one play session for one puzzle.

Suggested columns:

- `id` UUID primary key
- `puzzle_id` UUID not null references `puzzles(id)`
- `guild_id` VARCHAR null
- `channel_id` VARCHAR null
- `status` VARCHAR not null
- `mode` VARCHAR not null
- `validation_mode` VARCHAR not null
- `difficulty` VARCHAR not null
- `host_user_id` VARCHAR not null
- `created_by_user_id` VARCHAR not null
- `started_at` TIMESTAMP not null
- `updated_at` TIMESTAMP not null
- `completed_at` TIMESTAMP null

Notes:

- `guild_id` and `channel_id` are nullable so the design does not over-assume one Discord launch context
- `difficulty` is duplicated here intentionally for query convenience and historical accuracy

Recommended constraints:

- check that `status` is one of `pending`, `active`, `completed`, `cancelled`, `abandoned`
- check that `mode` is one of `single`, `collab`
- check that `validation_mode` is one of `guided`, `classic`

## 3. game_players

Stores which users belong to a game.

Suggested columns:

- `id` UUID primary key
- `game_id` UUID not null references `game_sessions(id)`
- `user_id` VARCHAR not null
- `role` VARCHAR not null
- `membership_status` VARCHAR not null
- `joined_at` TIMESTAMP not null
- `left_at` TIMESTAMP null
- `last_seen_at` TIMESTAMP null

Recommended constraints:

- unique constraint on `(game_id, user_id)`
- check that `role` is one of `host`, `participant`
- check that `membership_status` is one of `active`, `left`, `removed`

Recommended indexes:

- index on `user_id`
- index on `(game_id, membership_status)`

Important application rule:

- The database can enforce one player record per game, but the cross-game rule "one active game per user" is easiest to enforce in the service layer first

## 4. game_cells

Stores the mutable state of every board cell in a session.

Suggested columns:

- `game_id` UUID not null references `game_sessions(id)`
- `row_index` SMALLINT not null
- `column_index` SMALLINT not null
- `clue_value` SMALLINT null
- `entered_value` SMALLINT null
- `is_clue` BOOLEAN not null
- `last_updated_by` VARCHAR null
- `last_updated_at` TIMESTAMP null
- `last_feedback_type` VARCHAR null
- `last_feedback_at` TIMESTAMP null

Primary key:

- `(game_id, row_index, column_index)`

Recommended constraints:

- check that `row_index` is between 0 and 8
- check that `column_index` is between 0 and 8
- check that `clue_value` is null or between 1 and 9
- check that `entered_value` is null or between 1 and 9
- check that `last_feedback_type` is null or one of `correct`, `incorrect`

Notes:

- Each game should create 81 rows in this table
- `clue_value` remains fixed for clue cells
- `is_clue = true` means the cell is part of the original puzzle and is not editable
- `is_clue = true` should always be paired with a non-null `clue_value`
- `is_clue = false` should always be paired with a null `clue_value`
- `entered_value` stores the current user-entered value, even if it is incorrect in guided mode

## 5. game_pencil_marks

Stores pencil marks for editable cells.

Suggested columns:

- `game_id` UUID not null references `game_sessions(id)`
- `row_index` SMALLINT not null
- `column_index` SMALLINT not null
- `mark_set` VARCHAR not null
- `updated_by` VARCHAR null
- `updated_at` TIMESTAMP not null

Primary key:

- `(game_id, row_index, column_index)`

Recommended constraints:

- check that `row_index` is between 0 and 8
- check that `column_index` is between 0 and 8
- check that `mark_set` only contains digits `1-9` without duplicates

Notes:

- `mark_set` could hold values like `138` or `2459`
- An empty set can either mean no row exists or `mark_set = ''`; prefer no row for cleaner semantics

## 6. game_hints

Stores hint usage history.

Suggested columns:

- `id` UUID primary key
- `game_id` UUID not null references `game_sessions(id)`
- `used_by` VARCHAR not null
- `row_index` SMALLINT not null
- `column_index` SMALLINT not null
- `hint_type` VARCHAR not null
- `revealed_value` SMALLINT null
- `used_at` TIMESTAMP not null

Recommended constraints:

- check that `row_index` is between 0 and 8
- check that `column_index` is between 0 and 8
- check that `hint_type` is one of `reveal_cell`
- check that `revealed_value` is null or between 1 and 9

## Optional 7. game_feedback_events

Only needed if temporary guided-mode feedback should be persisted separately from cell state.

Suggested columns:

- `id` UUID primary key
- `game_id` UUID not null references `game_sessions(id)`
- `user_id` VARCHAR not null
- `row_index` SMALLINT not null
- `column_index` SMALLINT not null
- `feedback_type` VARCHAR not null
- `message` VARCHAR not null
- `created_at` TIMESTAMP not null
- `expires_at` TIMESTAMP not null

This table is optional for MVP1. Temporary feedback can instead be returned only in API responses.

## Storage Representation

### Board Encoding in puzzles

Use a compact 81-character string:

- `0` means empty in the clue board
- digits `1-9` represent set values

Benefits:

- easy import/export
- compact storage
- simple to validate

### Runtime Board State in game_cells

Use normalized per-cell rows:

- easier partial updates
- better auditability
- collaboration-ready
- easier compare-and-swap updates later

This split is intentional:

- puzzle catalog is compact
- live sessions are normalized

## Suggested Indexes

### puzzles

- index on `difficulty`

### game_sessions

- index on `status`
- index on `host_user_id`
- index on `(created_by_user_id, status)`
- index on `(guild_id, status)`

### game_players

- index on `user_id`
- index on `(game_id, membership_status)`

### game_cells

- primary key on `(game_id, row_index, column_index)` is sufficient for MVP1

### game_hints

- index on `game_id`

## Transaction Boundaries

The following operations should be transactional:

- game creation
  - insert into `game_sessions`
  - insert initial `game_players`
  - insert 81 `game_cells`
- final value entry
  - update `game_cells`
  - clear matching `game_pencil_marks`
  - optionally insert `game_feedback_events`
  - update `game_sessions.updated_at`
- hint usage
  - insert into `game_hints`
  - update `game_cells`
  - update `game_sessions.updated_at`

This matters even in solo mode because the schema is being shaped for future collaborative correctness.

## MVP1 vs Later

### Required for MVP1

- `puzzles`
- `game_sessions`
- `game_players`
- `game_cells`
- `game_pencil_marks`
- `game_hints`

### Optional for MVP1

- `game_feedback_events`

### Later

- leaderboard and rewards tables
- richer puzzle source/import metadata
- activity analytics

## Open Questions

- whether `guild_id` and `channel_id` are sufficient Discord context fields
- whether to persist temporary feedback events or keep them response-only
- whether `last_feedback_type` in `game_cells` is enough for MVP1
- whether puzzle difficulty should be fully trusted from imported data or recomputed later
