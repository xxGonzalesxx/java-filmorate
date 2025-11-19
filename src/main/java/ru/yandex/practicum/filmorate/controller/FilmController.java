package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final LocalDate valideDuration = LocalDate.of(1895, 12, 28);
    private final Map<Integer, Film> films = new HashMap();
    private int nextId = 1;

    //Получение всех фильмов
    @GetMapping
    public List<Film> getAll() {
        log.info("Получен запрос на получение всех фильмов. Текущее количество: {}", films.size());
        return new ArrayList<>(films.values());
    }

    //Добавление всех фильмов
    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film.getName());

        // Проверка названия
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: название не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }

        // Проверка описания
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: максимальная длина описания — 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        // Проверка продолжительности
        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации: продолжительность фильма должна быть положительным числом");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        // Проверка дата релиза — не раньше 28 декабря 1895 года;
        if (film.getReleaseDate().isBefore(valideDuration)) {
            log.error("Ошибка валидации: дата релиза не может быть раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм успешно создан с ID: {}, название: {}", film.getId(), film.getName());
        return film;
    }

    //Обновление фильма
    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с ID: {}", film.getId());

        // Проверка названия
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: название не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }

        // Проверка описания
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: максимальная длина описания — 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        // Проверка продолжительности
        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации: продолжительность фильма должна быть положительным числом");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        // Проверка дата релиза — не раньше 28 декабря 1895 года;
        if (film.getReleaseDate().isBefore(valideDuration)) {
            log.error("Ошибка валидации: дата релиза не может быть раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        films.put(film.getId(), film);
        log.info("Фильм с ID: {} успешно обновлен, название: {}", film.getId(), film.getName());
        return film;
    }
}