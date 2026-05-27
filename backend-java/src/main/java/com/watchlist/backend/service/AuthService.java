package com.watchlist.backend.service;

import com.watchlist.backend.errors.ApiException;
import com.watchlist.backend.repository.InMemoryUserRepository;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AuthService {
    private final InMemoryUserRepository userRepository;
    private final ShowService showService;

    public AuthService(InMemoryUserRepository userRepository, ShowService showService) {
        this.userRepository = userRepository;
        this.showService = showService;
    }

    public Map<String, Object> signup(String username, String password) {
        if (userRepository.exists(username)) {
            throw new ApiException(409, "That username is already taken.");
        }

        userRepository.insert(username, password);
        return buildAuthResponse(username);
    }

    public Map<String, Object> login(String username, String password) {
        if (!userRepository.matchesPassword(username, password)) {
            throw new ApiException(401, "Incorrect username or password.");
        }

        return buildAuthResponse(username);
    }

    private Map<String, Object> buildAuthResponse(String username) {
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("username", username);
        return response;
    }
}
