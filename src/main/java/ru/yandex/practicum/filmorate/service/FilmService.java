package ru.yandex.practicum.filmorate.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);
    private static final LocalDate VALID_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public List<Film> getAllFilms() {
        return filmStorage.findAll();
    }

    public Film getFilmById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    public Film createFilm(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        if (!filmStorage.existsById(film.getId())) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }
        validateFilm(film);
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        if (film.getLikedUserIds().contains(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }
        film.getLikedUserIds().add(userId);
        filmStorage.update(film);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        if (!film.getLikedUserIds().contains(userId)) {
            throw new NotFoundException("Лайк от пользователя " + userId + " не найден");
        }
        film.getLikedUserIds().remove(userId);
        filmStorage.update(film);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(f -> -f.getLikedUserIds().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: название не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: максимальная длина описания — 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации: продолжительность фильма должна быть положительным числом");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        if (film.getReleaseDate().isBefore(VALID_RELEASE_DATE)) {
            log.error("Ошибка валидации: дата релиза не может быть раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}
