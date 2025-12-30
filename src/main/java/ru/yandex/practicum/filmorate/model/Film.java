package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Film {
    private long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    // Новое поле
    private Mpa mpa;
    // Изменено с likedUserIds
    private Set<Genre> genres = new HashSet<>();
    private Set<Long> likedUserIds = new HashSet<>();
}