package com.watchlist.backend.validation;

import com.watchlist.backend.errors.ApiException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ShowValidator {
    private static final Set<String> SHOW_TYPES = Set.of("series", "movie", "anime", "documentary");
    private static final Set<String> SHOW_STATUSES = Set.of(
            "plan-to-watch",
            "watching",
            "completed",
            "on-hold",
            "dropped"
    );

    public LinkedHashMap<String, Object> validateCreatePayload(Map<String, Object> payload) {
        return validatePayload(payload, false);
    }

    public LinkedHashMap<String, Object> validateUpdatePayload(Map<String, Object> payload) {
        return validatePayload(payload, true);
    }

    public PaginationQuery validatePaginationQuery(String rawQuery) {
        Map<String, String> query = parseQuery(rawQuery);
        int page = parsePositiveInt(query.getOrDefault("page", "1"), "page");
        int limit = parsePositiveInt(query.getOrDefault("limit", "10"), "limit");

        if (limit > 100) {
            throw new ApiException(400, "limit must be an integer between 1 and 100.");
        }

        String status = blankToNull(query.get("status"));
        String search = blankToNull(query.get("search"));

        if (status != null && !SHOW_STATUSES.contains(status)) {
            throw new ApiException(400, "status must be one of: plan-to-watch, watching, completed, on-hold, dropped.");
        }

        return new PaginationQuery(page, limit, status, search);
    }

    private LinkedHashMap<String, Object> validatePayload(Map<String, Object> payload, boolean partial) {
        // partial = true  -> PATCH rules (some fields may be omitted)
        // partial = false -> POST/PUT rules (required fields must be present)
        if (payload == null) {
            throw new ApiException(400, "Request body must be a JSON object.");
        }

        if (partial && payload.isEmpty()) {
            throw new ApiException(400, "At least one field must be provided for partial updates.");
        }

        LinkedHashMap<String, Object> normalized = new LinkedHashMap<>();

        validateTitle(payload, normalized, partial);
        validateType(payload, normalized, partial);
        validateStatus(payload, normalized, partial);
        validateDescription(payload, normalized, partial);
        validateReleaseDate(payload, normalized, partial);
        validateImage(payload, normalized, partial);
        validateEpisodesWatched(payload, normalized, partial);
        validateTotalEpisodes(payload, normalized, partial);
        validateRating(payload, normalized, partial);
        validateGenres(payload, normalized, partial);

        Integer episodesWatched = (Integer) normalized.get("episodesWatched");
        Integer totalEpisodes = (Integer) normalized.get("totalEpisodes");

        if (episodesWatched != null && totalEpisodes != null && episodesWatched > totalEpisodes) {
            throw new ApiException(400, "episodesWatched cannot be greater than totalEpisodes.");
        }

        return normalized;
    }

    private void validateTitle(Map<String, Object> payload, Map<String, Object> normalized, boolean partial) {
        if (!partial || payload.containsKey("title")) {
            normalized.put("title", validateRequiredString(payload.get("title"), "title", 120));
        }
    }

    private void validateType(Map<String, Object> payload, Map<String, Object> normalized, boolean partial) {
        if (!partial || payload.containsKey("type")) {
            normalized.put("type", validateEnum(payload.get("type"), "type", SHOW_TYPES));
        }
    }

    private void validateStatus(Map<String, Object> payload, Map<String, Object> normalized, boolean partial) {
        if (!partial || payload.containsKey("status")) {
            normalized.put("status", validateEnum(payload.get("status"), "status", SHOW_STATUSES));
        }
    }

    private void validateDescription(Map<String, Object> payload, Map<String, Object> normalized, boolean partial) {
        if (!partial || payload.containsKey("description")) {
            normalized.put("description", validateOptionalString(payload.get("description"), "description", 300));
        }
    }

    private void validateReleaseDate(Map<String, Object> payload, Map<String, Object> normalized, boolean partial) {
        if (!partial || payload.containsKey("releaseDate")) {
            normalized.put("releaseDate", validateOptionalReleaseDate(payload.get("releaseDate")));
        }
    }

    private void validateImage(Map<String, Object> payload, Map<String, Object> normalized, boolean partial) {
        if (!partial || payload.containsKey("image")) {
            normalized.put("image", validateOptionalString(payload.get("image"), "image", 5_000_000));
        }
    }

    private void validateEpisodesWatched(Map<String, Object> payload, Map<String, Object> normalized, boolean partial) {
        if (!partial || payload.containsKey("episodesWatched")) {
            normalized.put("episodesWatched", validateOptionalInteger(payload.get("episodesWatched"), "episodesWatched"));
        }
    }

    private void validateTotalEpisodes(Map<String, Object> payload, Map<String, Object> normalized, boolean partial) {
        if (!partial || payload.containsKey("totalEpisodes")) {
            normalized.put("totalEpisodes", validateOptionalInteger(payload.get("totalEpisodes"), "totalEpisodes"));
        }
    }

    private void validateRating(Map<String, Object> payload, Map<String, Object> normalized, boolean partial) {
        if (!partial || payload.containsKey("rating")) {
            normalized.put("rating", validateOptionalRating(payload.get("rating")));
        }
    }

    private void validateGenres(Map<String, Object> payload, Map<String, Object> normalized, boolean partial) {
        if (!partial || payload.containsKey("genres")) {
            normalized.put("genres", validateOptionalGenres(payload.get("genres")));
        }
    }

    private String validateRequiredString(Object value, String fieldName, int maxLength) {
        if (!(value instanceof String stringValue)) {
            throw new ApiException(400, fieldName + " must be a string.");
        }

        String trimmed = stringValue.trim();

        if (trimmed.isEmpty()) {
            throw new ApiException(400, fieldName + " is required.");
        }

        if (trimmed.length() > maxLength) {
            throw new ApiException(400, fieldName + " must be at most " + maxLength + " characters.");
        }

        return trimmed;
    }

    private String validateOptionalString(Object value, String fieldName, int maxLength) {
        if (value == null) {
            return null;
        }

        if (!(value instanceof String stringValue)) {
            throw new ApiException(400, fieldName + " must be a string.");
        }

        String trimmed = stringValue.trim();

        if (trimmed.length() > maxLength) {
            throw new ApiException(400, fieldName + " must be at most " + maxLength + " characters.");
        }

        return trimmed;
    }

    private String validateOptionalReleaseDate(Object value) {
        String normalized = validateOptionalString(value, "releaseDate", 20);

        if (normalized == null || normalized.isEmpty()) {
            return normalized;
        }

        if (!normalized.matches("^(\\d{4}|\\d{4}-\\d{2}-\\d{2})$")) {
            throw new ApiException(400, "releaseDate must use YYYY or YYYY-MM-DD format.");
        }

        return normalized;
    }

    private String validateEnum(Object value, String fieldName, Set<String> allowedValues) {
        if (!(value instanceof String stringValue) || !allowedValues.contains(stringValue)) {
            throw new ApiException(400, fieldName + " must be one of: " + String.join(", ", allowedValues) + ".");
        }

        return stringValue;
    }

    private Integer validateOptionalInteger(Object value, String fieldName) {
        if (value == null) {
            return null;
        }

        if (!(value instanceof Number numberValue)) {
            throw new ApiException(400, fieldName + " must be a non-negative integer.");
        }

        double rawValue = numberValue.doubleValue();

        if (rawValue < 0 || rawValue != Math.floor(rawValue)) {
            throw new ApiException(400, fieldName + " must be a non-negative integer.");
        }

        return (int) rawValue;
    }

    private Double validateOptionalRating(Object value) {
        if (value == null) {
            return null;
        }

        if (!(value instanceof Number numberValue)) {
            throw new ApiException(400, "rating must be a number between 0 and 10.");
        }

        double rating = numberValue.doubleValue();

        if (rating < 0 || rating > 10) {
            throw new ApiException(400, "rating must be a number between 0 and 10.");
        }

        return rating;
    }

    private List<String> validateOptionalGenres(Object value) {
        if (value == null) {
            return null;
        }

        if (!(value instanceof List<?> genres)) {
            throw new ApiException(400, "genres must be an array of strings.");
        }

        List<String> normalized = new ArrayList<>();

        for (Object genre : genres) {
            normalized.add(validateRequiredString(genre, "genre", 40));
        }

        return normalized;
    }

    private int parsePositiveInt(String rawValue, String fieldName) {
        try {
            int value = Integer.parseInt(rawValue);

            if (value < 1) {
                throw new ApiException(400, fieldName + " must be a positive integer.");
            }

            return value;
        } catch (NumberFormatException exception) {
            throw new ApiException(400, fieldName + " must be a positive integer.");
        }
    }

    private Map<String, String> parseQuery(String rawQuery) {
        LinkedHashMap<String, String> query = new LinkedHashMap<>();

        if (rawQuery == null || rawQuery.isBlank()) {
            return query;
        }

        for (String pair : rawQuery.split("&")) {
            if (pair.isBlank()) {
                continue;
            }

            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            query.put(key, value);
        }

        return query;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record PaginationQuery(int page, int limit, String status, String search) {
    }
}
