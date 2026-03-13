package org.game.sudoku.repository;

import org.game.sudoku.entity.GameSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameSessionEntityRepository extends JpaRepository<GameSessionEntity, UUID> {
}
