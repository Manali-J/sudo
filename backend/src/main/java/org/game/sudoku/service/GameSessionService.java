package org.game.sudoku.service;

import lombok.extern.slf4j.Slf4j;
import org.game.sudoku.domain.enums.Difficulty;
import org.game.sudoku.domain.enums.GameMode;
import org.game.sudoku.domain.enums.GameStatus;
import org.game.sudoku.domain.enums.ValidationMode;
import org.game.sudoku.domain.model.GameSession;
import org.game.sudoku.dto.CreateGameRequest;
import org.game.sudoku.exceptions.GameException;
import org.game.sudoku.mapper.GameSessionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class GameSessionService {
    private final GameValidationService validationService;
    private final GameRepositoryService gameRepositoryService;
    private final GameSessionMapper gameSessionMapper;

    public GameSessionService(
            GameValidationService validationService,
            GameRepositoryService gameRepositoryService,
            GameSessionMapper gameSessionMapper) {
        this.validationService = validationService;
        this.gameRepositoryService = gameRepositoryService;
        this.gameSessionMapper = gameSessionMapper;
    }

    private GameSession createNewSession(UUID puzzleId,
                                         String guildId,
                                         String channelId,
                                         GameStatus gameStatus,
                                         GameMode gameMode,
                                         ValidationMode validationMode,
                                         Difficulty difficulty,
                                         String hostUserId,
                                         String createdByUserId) {
        return new GameSession(
                null,
                puzzleId,
                guildId,
                channelId,
                gameStatus,
                gameMode,
                validationMode,
                difficulty,
                hostUserId,
                createdByUserId,
                Instant.now(Clock.system(ZoneOffset.UTC)),
                Instant.now(Clock.system(ZoneOffset.UTC)),
                null);
    }

    @Transactional
    public GameSession createNewSession(CreateGameRequest request) throws GameException {
        // check if request is valid
        if (!validationService.validateCreateGameRequest(request)) {
            throw new IllegalArgumentException("Invalid create game request");
        }
        // find eligible puzzle for the game session
        var eligiblePuzzle =
                gameRepositoryService.getEligiblePuzzle(
                        request.difficulty(),
                        Set.of(GameStatus.PENDING, GameStatus.ACTIVE),
                        request.createdByUserId());

        // create new game session & game session entity from GameSession object
        var gameSession =
                createNewSession(
                        eligiblePuzzle.getPuzzleId(),
                        request.guildId(),
                        request.channelId(),
                        GameStatus.ACTIVE,
                        request.mode(),
                        request.validationMode(),
                        request.difficulty(),
                        request.hostUserId(),
                        request.createdByUserId());
        // create new session and persist
        var gameSessionEntity = gameSessionMapper.toEntity(gameSession);
        var savedEntity = gameRepositoryService.saveGameSession(gameSessionEntity);
        log.info(
                "Created new game session with ID: {} for puzzle ID: {} in guild: {} and channel: {}",
                savedEntity.getGameId(),
                eligiblePuzzle.getPuzzleId(),
                request.guildId(),
                request.channelId());
        return gameSessionMapper.toDomain(savedEntity);
    }
}
