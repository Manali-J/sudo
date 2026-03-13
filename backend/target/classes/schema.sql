CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS puzzle (
    puzzle_id UUID PRIMARY KEY,
    difficulty VARCHAR(16) NOT NULL,
    clue_board VARCHAR(81) NOT NULL,
    solution_board VARCHAR(81) NOT NULL,
    source VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_puzzle_difficulty
        CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'LETHAL')),
    CONSTRAINT chk_puzzle_clue_board_length
        CHECK (char_length(clue_board) = 81),
    CONSTRAINT chk_puzzle_solution_board_length
        CHECK (char_length(solution_board) = 81),
    CONSTRAINT uq_puzzle_solution_board UNIQUE (solution_board)
);

CREATE TABLE IF NOT EXISTS game_session (
    game_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    puzzle_id UUID NOT NULL,
    guild_id VARCHAR(32) NOT NULL,
    channel_id VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    mode VARCHAR(16) NOT NULL,
    validation_mode VARCHAR(16) NOT NULL,
    difficulty VARCHAR(16) NOT NULL,
    host_user_id VARCHAR(32) NOT NULL,
    created_by_user_id VARCHAR(32) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_game_session_puzzle
        FOREIGN KEY (puzzle_id) REFERENCES puzzle (puzzle_id),
    CONSTRAINT chk_game_session_status
        CHECK (status IN ('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'ABANDONED')),
    CONSTRAINT chk_game_session_mode
        CHECK (mode IN ('SINGLE', 'COLLAB')),
    CONSTRAINT chk_game_session_validation_mode
        CHECK (validation_mode IN ('GUIDED', 'CLASSIC')),
    CONSTRAINT chk_game_session_difficulty
        CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'LETHAL'))
);

CREATE TABLE IF NOT EXISTS player_session (
    player_session_id UUID PRIMARY KEY,
    game_id UUID NOT NULL,
    user_id VARCHAR(32) NOT NULL,
    role VARCHAR(16) NOT NULL,
    membership_status VARCHAR(16) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,
    left_at TIMESTAMPTZ,
    last_seen_at TIMESTAMPTZ,
    CONSTRAINT fk_player_session_game
        FOREIGN KEY (game_id) REFERENCES game_session (game_id) ON DELETE CASCADE,
    CONSTRAINT uq_player_session_game_user UNIQUE (game_id, user_id),
    CONSTRAINT chk_player_session_role
        CHECK (role IN ('HOST', 'PARTICIPANT')),
    CONSTRAINT chk_player_session_membership_status
        CHECK (membership_status IN ('ACTIVE', 'LEFT', 'REMOVED'))
);

CREATE TABLE IF NOT EXISTS cell_state (
    game_id UUID NOT NULL,
    row_index INTEGER NOT NULL,
    column_index INTEGER NOT NULL,
    clue_value INTEGER,
    entered_value INTEGER,
    is_clue BOOLEAN NOT NULL,
    last_updated_by VARCHAR(32),
    last_updated_at TIMESTAMPTZ,
    last_feedback_type VARCHAR(16),
    last_feedback_at TIMESTAMPTZ,
    PRIMARY KEY (game_id, row_index, column_index),
    CONSTRAINT fk_cell_state_game
        FOREIGN KEY (game_id) REFERENCES game_session (game_id) ON DELETE CASCADE,
    CONSTRAINT chk_cell_state_row
        CHECK (row_index BETWEEN 0 AND 8),
    CONSTRAINT chk_cell_state_column
        CHECK (column_index BETWEEN 0 AND 8),
    CONSTRAINT chk_cell_state_clue_value
        CHECK (clue_value IS NULL OR clue_value BETWEEN 1 AND 9),
    CONSTRAINT chk_cell_state_entered_value
        CHECK (entered_value IS NULL OR entered_value BETWEEN 1 AND 9),
    CONSTRAINT chk_cell_state_feedback_type
        CHECK (last_feedback_type IS NULL OR last_feedback_type IN ('CORRECT', 'INCORRECT'))
);

CREATE TABLE IF NOT EXISTS pencil_marks (
    game_id UUID NOT NULL,
    row_index INTEGER NOT NULL,
    column_index INTEGER NOT NULL,
    mark_set INTEGER[] NOT NULL DEFAULT '{}',
    updated_by VARCHAR(32) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (game_id, row_index, column_index),
    CONSTRAINT fk_pencil_marks_game
        FOREIGN KEY (game_id) REFERENCES game_session (game_id) ON DELETE CASCADE,
    CONSTRAINT chk_pencil_marks_row
        CHECK (row_index BETWEEN 0 AND 8),
    CONSTRAINT chk_pencil_marks_column
        CHECK (column_index BETWEEN 0 AND 8)
);

CREATE TABLE IF NOT EXISTS hint_usage (
    hint_id UUID PRIMARY KEY,
    game_id UUID NOT NULL,
    used_by VARCHAR(32) NOT NULL,
    row_index INTEGER NOT NULL,
    column_index INTEGER NOT NULL,
    hint_type VARCHAR(32) NOT NULL,
    revealed_value INTEGER,
    used_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_hint_usage_game
        FOREIGN KEY (game_id) REFERENCES game_session (game_id) ON DELETE CASCADE,
    CONSTRAINT chk_hint_usage_row
        CHECK (row_index BETWEEN 0 AND 8),
    CONSTRAINT chk_hint_usage_column
        CHECK (column_index BETWEEN 0 AND 8),
    CONSTRAINT chk_hint_usage_type
        CHECK (hint_type IN ('REVEAL_CELL')),
    CONSTRAINT chk_hint_usage_revealed_value
        CHECK (revealed_value IS NULL OR revealed_value BETWEEN 1 AND 9)
);
