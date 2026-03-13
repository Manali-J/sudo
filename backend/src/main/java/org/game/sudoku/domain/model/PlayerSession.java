package org.game.sudoku.domain.model;

import org.game.sudoku.domain.enums.MembershipStatus;
import org.game.sudoku.domain.enums.PlayerRole;

import java.time.Instant;
import java.util.UUID;

public record PlayerSession(
        UUID playerSessionId,
        UUID gameId,
        String userId,
        PlayerRole role,
        MembershipStatus membershipStatus,
        Instant joinedAt,
        Instant leftAt,
        Instant lastSeenAt
) {
}
