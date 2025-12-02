package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {

    @Autowired
    private FilmController filmController;

    @Test
    void createFilm_WithValidData_ShouldSuccess() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals("Valid Film", createdFilm.getName());
    }

    @Test
    void createFilm_WithEmptyName_ShouldThrowException() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void createFilm_WithLongDescription_ShouldThrowException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A".repeat(201)); // 201 символов
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void createFilm_WithInvalidReleaseDate_ShouldThrowException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(1890, 1, 1)); // До 1895-12-28
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void createFilm_WithNegativeDuration_ShouldThrowException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-10);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }
}