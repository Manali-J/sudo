package org.game.sudoku.domain.model;

import org.game.sudoku.domain.enums.Difficulty;

import java.time.Instant;
import java.util.UUID;

public record Puzzle(
        UUID puzzleId,
        Difficulty difficulty,
        String clueBoard,
        String solutionBoard,
        String source,
        Instant createdAt
) {
}
