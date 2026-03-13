package org.game.sudoku.domain.model;

import org.game.sudoku.domain.enums.HintType;

import java.time.Instant;
import java.util.UUID;

public record HintUsage(
        UUID hintId,
        UUID gameId,
        String usedBy,
        int rowIndex,
        int columnIndex,
        HintType hintType,
        Integer revealedValue,
        Instant usedAt
) {
}
