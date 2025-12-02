package ru.yandex.practicum.filmorate.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        return userStorage.findAll();
    }

    public User getUserById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    public User createUser(User user) {
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        if (!userStorage.existsById(user.getId())) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.update(user);
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить себя в друзья");
        }

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriendIds().contains(friendId)) {
            throw new ValidationException("Пользователь уже добавлен в друзья");
        }

        user.getFriendIds().add(friendId);
        friend.getFriendIds().add(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователь {} и пользователь {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);


        user.getFriendIds().remove(friendId);
        friend.getFriendIds().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователь {} и пользователь {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        User user = getUserById(userId);
        return user.getFriendIds().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = getUserById(userId);
        User other = getUserById(otherId);

        Set<Long> commonFriendIds = new HashSet<>(user.getFriendIds());
        commonFriendIds.retainAll(other.getFriendIds());

        return commonFriendIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Ошибка валидации: электронная почта не может быть пустой");
            throw new ValidationException("Электронная почта не может быть пустой");
        }

        if (!user.getEmail().contains("@")) {
            log.error("Ошибка валидации: электронная почта должна содержать символ @");
            throw new ValidationException("Электронная почта должна содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("Ошибка валидации: логин не может быть пустым");
            throw new ValidationException("Логин не может быть пустым");
        }

        if (user.getLogin().contains(" ")) {
            log.error("Ошибка валидации: логин не может содержать пробелы");
            throw new ValidationException("Логин не может содержать пробелы");
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации: дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}