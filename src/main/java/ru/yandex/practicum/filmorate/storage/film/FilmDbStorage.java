package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        
        long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);
        
        // Сохраняем жанры
        saveGenres(filmId, film.getGenres());
        
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        
        // Обновляем жанры
        updateGenres(film.getId(), film.getGenres());
        
        return film;
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT f.* FROM films f ORDER BY f.id";
        List<Film> films = jdbcTemplate.query(sql, filmMapper);
        
        // Загружаем дополнительные данные для каждого фильма
        films.forEach(this::loadAdditionalData);
        
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT f.* FROM films f WHERE f.id = ?";
        List<Film> films = jdbcTemplate.query(sql, filmMapper, id);
        
        if (films.isEmpty()) {
            return Optional.empty();
        }
        
        Film film = films.get(0);
        loadAdditionalData(film);
        return Optional.of(film);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private void saveGenres(long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        
        for (Genre genre : genres) {
            jdbcTemplate.update(sql, filmId, genre.getId());
        }
    }

    private void updateGenres(long filmId, Set<Genre> genres) {
        // Удаляем старые жанры
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);
        
        // Добавляем новые
        saveGenres(filmId, genres);
    }

    private void loadAdditionalData(Film film) {
        loadMpa(film);
        loadGenres(film);
        loadLikes(film);
    }

    private void loadMpa(Film film) {
        String sql = "SELECT m.id, m.name FROM mpa m WHERE m.id = ?";
        Mpa mpa = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Mpa m = new Mpa();
            m.setId(rs.getInt("id"));
            m.setName(rs.getString("name"));
            return m;
        }, film.getMpa().getId());
        
        film.setMpa(mpa);
    }

    private void loadGenres(Film film) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                    "JOIN film_genres fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id = ? ORDER BY g.id";
        
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());
        
        film.setGenres(new HashSet<>(genres));
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"), film.getId());
        film.setLikedUserIds(new HashSet<>(likes));
    }

    // Методы для работы с лайками
    public void addLike(long filmId, long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, COUNT(l.user_id) as likes_count " +
                    "FROM films f " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "GROUP BY f.id " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";
        
        List<Film> films = jdbcTemplate.query(sql, filmMapper, count);
        films.forEach(this::loadAdditionalData);
        return films;
    }
}
