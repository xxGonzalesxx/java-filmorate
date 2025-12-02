package ru.yandex.practicum.filmorate.storage.user;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public User create(User user) {
        user.setId(nextId.getAndIncrement());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }
}