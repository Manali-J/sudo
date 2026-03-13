package org.game.sudoku.exceptions;

import lombok.Getter;

@Getter
public class GameException extends Exception {
    private final GameExceptionCode code;

    public GameException(GameExceptionCode code, String message) {
        super(message);
        this.code = code;
    }

}
