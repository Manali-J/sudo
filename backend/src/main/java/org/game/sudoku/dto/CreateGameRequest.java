package org.game.sudoku.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.game.sudoku.domain.enums.Difficulty;
import org.game.sudoku.domain.enums.GameMode;
import org.game.sudoku.domain.enums.ValidationMode;

public record CreateGameRequest(
        @NotNull GameMode mode,
        @NotNull Difficulty difficulty,
        @NotNull ValidationMode validationMode,
        @NotBlank String guildId,
        @NotBlank String channelId,
        @NotBlank String hostUserId,
        @NotBlank String createdByUserId
) {
}
