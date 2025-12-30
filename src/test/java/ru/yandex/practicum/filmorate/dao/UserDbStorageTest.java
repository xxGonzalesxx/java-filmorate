package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ComponentScan(basePackages = {"ru.yandex.practicum.filmorate.dao.mapper", 
                               "ru.yandex.practicum.filmorate.storage.user"})
class UserDbStorageTest {
    private final UserStorage userStorage;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setLogin("testLogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void testCreateUser() {
        User createdUser = userStorage.create(testUser);
        
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isPositive();
        assertThat(createdUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(createdUser.getLogin()).isEqualTo(testUser.getLogin());
        assertThat(createdUser.getName()).isEqualTo(testUser.getName());
        assertThat(createdUser.getBirthday()).isEqualTo(testUser.getBirthday());
    }

    @Test
    void testUpdateUser() {
        User createdUser = userStorage.create(testUser);
        
        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@example.com");
        
        User updatedUser = userStorage.update(createdUser);
        
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void testFindUserById() {
        User createdUser = userStorage.create(testUser);
        
        Optional<User> foundUser = userStorage.findById(createdUser.getId());
        
        assertThat(foundUser)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", createdUser.getId())
                );
    }

    @Test
    void testFindAllUsers() {
        userStorage.create(testUser);
        
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setLogin("anotherLogin");
        anotherUser.setName("Another User");
        anotherUser.setBirthday(LocalDate.of(1995, 5, 5));
        userStorage.create(anotherUser);
        
        List<User> users = userStorage.findAll();
        
        assertThat(users).hasSize(2);
    }
}
