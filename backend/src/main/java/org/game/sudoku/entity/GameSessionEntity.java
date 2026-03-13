package org.game.sudoku.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.game.sudoku.domain.enums.Difficulty;
import org.game.sudoku.domain.enums.GameMode;
import org.game.sudoku.domain.enums.GameStatus;
import org.game.sudoku.domain.enums.ValidationMode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "game_session")
public class GameSessionEntity {
    @Id
    @GeneratedValue
    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Column(name = "puzzle_id", nullable = false)
    private UUID puzzleId;

    @Column(name = "guild_id", nullable = false, length = 32)
    private String guildId;

    @Column(name = "channel_id", nullable = false, length = 32)
    private String channelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private GameStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 16)
    private GameMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_mode", nullable = false, length = 16)
    private ValidationMode validationMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 16)
    private Difficulty difficulty;

    @Column(name = "host_user_id", nullable = false, length = 32)
    private String hostUserId;

    @Column(name = "created_by_user_id", nullable = false, length = 32)
    private String createdByUserId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
