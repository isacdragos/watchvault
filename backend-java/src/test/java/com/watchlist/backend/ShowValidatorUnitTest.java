package com.watchlist.backend;

import com.watchlist.backend.errors.ApiException;
import com.watchlist.backend.validation.ShowValidator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ShowValidatorUnitTest {
    private ShowValidatorUnitTest() {
    }

    public static void runAll() {
        ShowValidator validator = new ShowValidator();

        shouldAcceptValidCreatePayload(validator);
        shouldRejectEmptyPatchPayload(validator);
        shouldRejectInvalidReleaseDate(validator);
        shouldRejectEpisodesGreaterThanTotal(validator);
        shouldValidatePaginationRules(validator);
    }

    private static void shouldAcceptValidCreatePayload(ShowValidator validator) {
        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", "Arcane");
        payload.put("type", "series");
        payload.put("status", "watching");
        payload.put("description", "Animated fantasy drama.");
        payload.put("releaseDate", "2021");
        payload.put("image", "data:image/png;base64,abc");
        payload.put("episodesWatched", 3);
        payload.put("totalEpisodes", 9);
        payload.put("rating", 9.5);
        payload.put("genres", List.of("Animation", "Fantasy"));

        LinkedHashMap<String, Object> validated = validator.validateCreatePayload(payload);

        assertEquals("Arcane", validated.get("title"));
        assertEquals("2021", validated.get("releaseDate"));
        assertEquals(9.5, validated.get("rating"));
    }

    private static void shouldRejectEmptyPatchPayload(ShowValidator validator) {
        expectApiException(
                () -> validator.validateUpdatePayload(new LinkedHashMap<>()),
                400,
                "At least one field must be provided for partial updates."
        );
    }

    private static void shouldRejectInvalidReleaseDate(ShowValidator validator) {
        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", "Arcane");
        payload.put("type", "series");
        payload.put("status", "watching");
        payload.put("releaseDate", "20-10-01");

        expectApiException(
                () -> validator.validateCreatePayload(payload),
                400,
                "releaseDate must use YYYY or YYYY-MM-DD format."
        );
    }

    private static void shouldRejectEpisodesGreaterThanTotal(ShowValidator validator) {
        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", "Arcane");
        payload.put("type", "series");
        payload.put("status", "watching");
        payload.put("episodesWatched", 10);
        payload.put("totalEpisodes", 9);

        expectApiException(
                () -> validator.validateCreatePayload(payload),
                400,
                "episodesWatched cannot be greater than totalEpisodes."
        );
    }

    private static void shouldValidatePaginationRules(ShowValidator validator) {
        ShowValidator.PaginationQuery query = validator.validatePaginationQuery("page=2&limit=5&status=watching&search=arcane");

        assertEquals(2, query.page());
        assertEquals(5, query.limit());
        assertEquals("watching", query.status());
        assertEquals("arcane", query.search());

        expectApiException(
                () -> validator.validatePaginationQuery("page=0"),
                400,
                "page must be a positive integer."
        );
    }

    private static void expectApiException(ThrowingRunnable action, int expectedStatus, String expectedMessage) {
        try {
            action.run();
            throw new AssertionError("Expected ApiException to be thrown.");
        } catch (ApiException exception) {
            assertEquals(expectedStatus, exception.getStatusCode());
            assertEquals(expectedMessage, exception.getMessage());
        } catch (Exception exception) {
            throw new AssertionError("Expected ApiException but got " + exception.getClass().getSimpleName(), exception);
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected instanceof Number expectedNumber && actual instanceof Number actualNumber) {
            double difference = Math.abs(expectedNumber.doubleValue() - actualNumber.doubleValue());

            if (difference > 0.000001d) {
                throw new AssertionError("Expected " + expected + " but got " + actual);
            }

            return;
        }

        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
