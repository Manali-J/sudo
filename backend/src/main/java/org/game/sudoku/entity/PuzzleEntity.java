package org.game.sudoku.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.game.sudoku.domain.enums.Difficulty;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "puzzle")
public class PuzzleEntity {
    @Id
    @Column(name = "puzzle_id", nullable = false)
    private UUID puzzleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 16)
    private Difficulty difficulty;

    @Column(name = "clue_board", nullable = false, length = 81)
    private String clueBoard;

    @Column(name = "solution_board", nullable = false, length = 81)
    private String solutionBoard;

    @Column(name = "source")
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
