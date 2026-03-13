package org.game.sudoku.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record PencilMarks(
        UUID gameId,
        int rowIndex,
        int columnIndex,
        Set<Integer> markSet,
        String updatedBy,
        Instant updatedAt
) {
}
