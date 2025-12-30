package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final LocalDate VALID_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserService userService;

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
        // Проверяем что фильм и пользователь существуют
        getFilmById(filmId);
        userService.getUserById(userId);

        // Используем методы FilmDbStorage
        if (filmStorage instanceof ru.yandex.practicum.filmorate.storage.film.FilmDbStorage) {
            ru.yandex.practicum.filmorate.storage.film.FilmDbStorage dbStorage = 
                (ru.yandex.practicum.filmorate.storage.film.FilmDbStorage) filmStorage;
            dbStorage.addLike(filmId, userId);
        } else {
            throw new ValidationException("Метод добавления лайка не поддерживается");
        }

        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        // Проверяем что фильм и пользователь существуют
        getFilmById(filmId);
        userService.getUserById(userId);

        // Используем методы FilmDbStorage
        if (filmStorage instanceof ru.yandex.practicum.filmorate.storage.film.FilmDbStorage) {
            ru.yandex.practicum.filmorate.storage.film.FilmDbStorage dbStorage = 
                (ru.yandex.practicum.filmorate.storage.film.FilmDbStorage) filmStorage;
            dbStorage.removeLike(filmId, userId);
        } else {
            throw new ValidationException("Метод удаления лайка не поддерживается");
        }

        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        if (filmStorage instanceof ru.yandex.practicum.filmorate.storage.film.FilmDbStorage) {
            ru.yandex.practicum.filmorate.storage.film.FilmDbStorage dbStorage = 
                (ru.yandex.practicum.filmorate.storage.film.FilmDbStorage) filmStorage;
            return dbStorage.getPopularFilms(count);
        } else {
            throw new ValidationException("Метод получения популярных фильмов не поддерживается");
        }
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
        
        if (film.getMpa() == null) {
            log.error("Ошибка валидации: рейтинг MPA должен быть указан");
            throw new ValidationException("Рейтинг MPA должен быть указан");
        }
    }
}
