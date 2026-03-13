package org.game.sudoku.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.game.sudoku.exceptions.GameException;
import org.game.sudoku.exceptions.GameExceptionCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(GameException.class)
    public ResponseEntity<ApiErrorResponse> handleGameException(
            GameException exception, HttpServletRequest request) {
        var status = mapStatus(exception.getCode());
        return ResponseEntity.status(status)
                .body(buildResponse(status, exception.getCode().name(), exception.getMessage(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        var message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request));
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            Exception exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred",
                        request
                ));
    }

    private HttpStatus mapStatus(GameExceptionCode code) {
        return switch (code) {
            case NO_ELIGIBLE_PUZZLE_AVAILABLE, INVALID_ACTION, INVALID_CELL, INVALID_VALUE ->
                    HttpStatus.BAD_REQUEST;
            case GAME_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FORBIDDEN_ACTION, CLUE_CELL_EDIT_FORBIDDEN -> HttpStatus.FORBIDDEN;
            case ACTIVE_GAME_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case GAME_NOT_IN_PROGRESS, UNSUPPORTED_MODE -> HttpStatus.UNPROCESSABLE_CONTENT;
        };
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }

    private ApiErrorResponse buildResponse(
            HttpStatus status, String code, String message, HttpServletRequest request) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI()
        );
    }

    public record ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String code,
            String message,
            String path
    ) {
    }
}
