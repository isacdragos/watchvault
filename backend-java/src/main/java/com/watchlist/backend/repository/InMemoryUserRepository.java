package com.watchlist.backend.repository;

import java.util.LinkedHashMap;
import java.util.Map;

public final class InMemoryUserRepository {
    private final Map<String, String> usersByUsername = new LinkedHashMap<>();

    public synchronized boolean exists(String username) {
        return usersByUsername.containsKey(normalizeKey(username));
    }

    public synchronized void insert(String username, String password) {
        usersByUsername.put(normalizeKey(username), password);
    }

    public synchronized boolean matchesPassword(String username, String password) {
        String storedPassword = usersByUsername.get(normalizeKey(username));
        return storedPassword != null && storedPassword.equals(password);
    }

    private String normalizeKey(String username) {
        return username.toLowerCase();
    }
}
