package com.watchlist.backend.validation;

import com.watchlist.backend.errors.ApiException;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AuthValidator {
    public Credentials validateCredentials(Map<String, Object> payload, boolean requirePasswordConfirmation) {
        if (payload == null) {
            throw new ApiException(400, "Request body must be a JSON object.");
        }

        String username = validateUsername(payload.get("username"));
        String password = validatePassword(payload.get("password"));

        if (requirePasswordConfirmation) {
            String confirmPassword = validatePassword(payload.get("confirmPassword"));

            if (!password.equals(confirmPassword)) {
                throw new ApiException(400, "Passwords do not match.");
            }
        }

        return new Credentials(username, password);
    }

    private String validateUsername(Object value) {
        if (!(value instanceof String stringValue)) {
            throw new ApiException(400, "Username must be a string.");
        }

        String normalized = stringValue.trim();

        if (normalized.length() < 3) {
            throw new ApiException(400, "Username must have at least 3 characters.");
        }

        return normalized;
    }

    private String validatePassword(Object value) {
        if (!(value instanceof String stringValue)) {
            throw new ApiException(400, "Password must be a string.");
        }

        if (stringValue.length() < 6) {
            throw new ApiException(400, "Password must have at least 6 characters.");
        }

        return stringValue;
    }

    public record Credentials(String username, String password) {
    }
}
