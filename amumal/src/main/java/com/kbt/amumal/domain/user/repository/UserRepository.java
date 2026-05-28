package com.kbt.amumal.domain.user.repository;

import com.kbt.amumal.domain.user.entity.User;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class UserRepository {
    private final Map<Integer, User> store = new HashMap<>();
    private final AtomicInteger countingUserId = new AtomicInteger(1);

    public User save(User user) {
        int newId = countingUserId.getAndIncrement();
        User savedUser = new User(newId, user.getEmail(), user.getPassword(), user.getNickname(), user.getProfileImageUrl());
        store.put(newId, savedUser);

        return savedUser;
    }

    public Optional<User> findByEmail(String email) {
        return store.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }
}