package org.game.sudoku.mapper;

import org.game.sudoku.domain.model.GameSession;
import org.game.sudoku.entity.GameSessionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GameSessionMapper {
    GameSessionEntity toEntity(GameSession gameSession);

    GameSession toDomain(GameSessionEntity entity);
}
