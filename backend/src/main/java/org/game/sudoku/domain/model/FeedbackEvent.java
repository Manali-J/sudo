package org.game.sudoku.domain.model;

import org.game.sudoku.domain.enums.FeedbackType;

import java.time.Instant;
import java.util.UUID;

public record FeedbackEvent(
        UUID feedbackId,
        UUID gameId,
        String userId,
        int rowIndex,
        int columnIndex,
        FeedbackType feedbackType,
        String message,
        Instant createdAt,
        Instant expiresAt
) {
}
