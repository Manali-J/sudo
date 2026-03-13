package org.game.sudoku.repository;

import org.game.sudoku.domain.enums.Difficulty;
import org.game.sudoku.domain.enums.GameStatus;
import org.game.sudoku.entity.PuzzleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PuzzleRepository extends JpaRepository<PuzzleEntity, UUID> {
    @Query("""
        select p
        from PuzzleEntity p
        where p.difficulty = :difficulty
          and not exists (
              select 1
              from GameSessionEntity gs
              where gs.puzzleId = p.puzzleId
                and gs.status in :inProgressStatuses
          )
          and not exists (
              select 1
              from GameSessionEntity gs, PlayerSessionEntity ps
              where gs.gameId = ps.gameId
                and gs.puzzleId = p.puzzleId
                and ps.userId = :userId
          )
        order by p.createdAt
        """)
    List<PuzzleEntity> findEligiblePuzzles(
            Difficulty difficulty,
            Set<GameStatus> inProgressStatuses,
            String userId
    );
}
