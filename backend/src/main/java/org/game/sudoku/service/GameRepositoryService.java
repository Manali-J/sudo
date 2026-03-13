package org.game.sudoku.service;

import org.game.sudoku.domain.enums.Difficulty;
import org.game.sudoku.domain.enums.GameStatus;
import org.game.sudoku.entity.GameSessionEntity;
import org.game.sudoku.entity.PuzzleEntity;
import org.game.sudoku.exceptions.GameException;
import org.game.sudoku.exceptions.GameExceptionCode;
import org.game.sudoku.repository.GameSessionEntityRepository;
import org.game.sudoku.repository.PuzzleRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class GameRepositoryService {
    private final PuzzleRepository puzzleRepository;
    private final GameSessionEntityRepository gameSessionRepository;

    public GameRepositoryService(PuzzleRepository puzzleRepository, GameSessionEntityRepository gameSessionRepository) {
        this.puzzleRepository = puzzleRepository;
        this.gameSessionRepository = gameSessionRepository;
    }

    public PuzzleEntity getEligiblePuzzle(
            Difficulty difficulty, Set<GameStatus> excludedStatuses, String userId) throws GameException {
        return puzzleRepository.findEligiblePuzzles(difficulty, excludedStatuses, userId).stream()
                .findFirst()
                .orElseThrow(() -> new GameException(GameExceptionCode.NO_ELIGIBLE_PUZZLE_AVAILABLE,
                        "No eligible puzzle available for the specified difficulty and user"));
    }

    public GameSessionEntity saveGameSession(GameSessionEntity gameSession) {
        return gameSessionRepository.save(gameSession);
    }
}
