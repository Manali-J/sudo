package org.game.sudoku.controller;

import jakarta.validation.Valid;
import org.game.sudoku.domain.model.GameSession;
import org.game.sudoku.dto.CreateGameRequest;
import org.game.sudoku.exceptions.GameException;
import org.game.sudoku.service.GameSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/game")
public class GameController {
    private final GameSessionService gameSessionService;

    public GameController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @PostMapping(value = "create")
    public ResponseEntity<GameSession> create(@Valid @RequestBody CreateGameRequest request) throws GameException {
        GameSession gameSession = gameSessionService.createNewSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameSession);
    }
}
