package org.game.sudoku.repository;

import org.game.sudoku.entity.PlayerSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerSessionRepository extends JpaRepository<PlayerSessionEntity, UUID> {
}
