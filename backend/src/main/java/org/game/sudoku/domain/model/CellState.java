package org.game.sudoku.domain.model;

import org.game.sudoku.domain.enums.FeedbackType;

import java.time.Instant;
import java.util.UUID;

public record CellState(
        UUID gameId,
        int rowIndex,
        int columnIndex,
        Integer clueValue,
        Integer enteredValue,
        boolean isClue,
        String lastUpdatedBy,
        Instant lastUpdatedAt,
        FeedbackType lastFeedbackType,
        Instant lastFeedbackAt
) {
}
