package org.game.sudoku.domain.model;

import org.game.sudoku.domain.enums.Difficulty;
import org.game.sudoku.domain.enums.GameMode;
import org.game.sudoku.domain.enums.GameStatus;
import org.game.sudoku.domain.enums.ValidationMode;

import java.time.Instant;
import java.util.UUID;

public record GameSession(
        UUID gameId,
        UUID puzzleId,
        String guildId,
        String channelId,
        GameStatus status,
        GameMode mode,
        ValidationMode validationMode,
        Difficulty difficulty,
        String hostUserId,
        String createdByUserId,
        Instant startedAt,
        Instant updatedAt,
        Instant completedAt
) {
}
