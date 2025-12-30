package ru.yandex.practicum.filmorate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilmorateApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilmorateApplication.class, args);
        System.out.println("Сервер работает на порту 8080");
        System.out.println("Ссылка для фильмов: http://localhost:8080/films");
        System.out.println("Ссылка для пользователей: http://localhost:8080/users");
    }
}