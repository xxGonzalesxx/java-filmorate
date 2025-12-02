package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(final ValidationException e) {
        return Map.of(
                "error", "Ошибка валидации",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
        );
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(final NotFoundException e) {
        return Map.of(
                "error", "Объект не найден",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
        );
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleThrowable(final Throwable e) {
        return Map.of(
                "error", "Внутренняя ошибка сервера",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
        );
    }
}