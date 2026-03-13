package org.game.sudoku.service;

import org.game.sudoku.domain.enums.GameMode;
import org.game.sudoku.dto.CreateGameRequest;
import org.game.sudoku.exceptions.GameException;
import org.game.sudoku.exceptions.GameExceptionCode;
import org.springframework.stereotype.Service;

@Service
public class GameValidationService {
    public boolean validateCreateGameRequest(CreateGameRequest request) throws GameException {
        final var mode = request.mode();
        final var difficulty = request.difficulty();
        final var validationMode = request.validationMode();
        final var guildId = request.guildId();
        final var channelId = request.channelId();
        final var hostUserId = request.hostUserId();
        final var createdByUserId = request.createdByUserId();

        if (mode == null || difficulty == null || validationMode == null) {
            return false;
        }
        if (guildId.isBlank()
                || channelId.isBlank()
                || hostUserId.isBlank()
                || createdByUserId.isBlank()) {
            return false;
        }

        //check that game mode should be SINGLE, if not then throw unsupported game mode exception
        if (mode != GameMode.SINGLE) {
            throw new GameException(GameExceptionCode.UNSUPPORTED_MODE, "Only SINGLE mode is supported for now");
        }
        return true;
    }
}
