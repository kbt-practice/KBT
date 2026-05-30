package com.kbt.amumal.domain.user.repository;

import com.kbt.amumal.domain.user.entity.User;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepository {
    private final Map<String, User> store = new HashMap<>();

    public User save(User user) {
        String newId = UUID.randomUUID().toString();
        User savedUser = new User(newId, user.getEmail(), user.getPassword(), user.getNickname(), user.getProfileImageUrl());
        store.put(newId, savedUser);

        return savedUser;
    }

    public Optional<User> findByEmail(String email) {
        return store.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    public Optional<User> findById(String userId) {
        return Optional.ofNullable(store.get(userId));
    }
}