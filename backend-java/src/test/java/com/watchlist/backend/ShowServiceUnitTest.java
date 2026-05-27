package com.watchlist.backend;

import com.watchlist.backend.repository.InMemoryShowRepository;
import com.watchlist.backend.service.ShowService;
import com.watchlist.backend.validation.ShowValidator;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ShowServiceUnitTest {
    private ShowServiceUnitTest() {
    }

    public static void runAll() {
        shouldCreateListAndPatchShows();
        shouldBuildStatsFromStoredShows();
    }

    private static void shouldCreateListAndPatchShows() {
        ShowService service = new ShowService(new InMemoryShowRepository());
        String username = "tester";

        Map<String, Object> created = service.createShow(username, createPayload("Arcane", "watching", 3, 9, 9.5));
        assertEquals("1", created.get("id"));

        ShowValidator.PaginationQuery query = new ShowValidator.PaginationQuery(1, 10, "watching", "arc");
        Map<String, Object> listResponse = service.listShows(username, query);
        assertEquals(1, ((java.util.List<?>) listResponse.get("items")).size());

        Map<String, Object> patchPayload = new LinkedHashMap<>();
        patchPayload.put("status", "completed");
        patchPayload.put("rating", 10.0);

        Map<String, Object> updated = service.updateShow(username, "1", patchPayload);
        assertEquals("completed", updated.get("status"));
        assertEquals(10.0, updated.get("rating"));
        assertEquals("Arcane", updated.get("title"));
    }

    private static void shouldBuildStatsFromStoredShows() {
        ShowService service = new ShowService(new InMemoryShowRepository());
        String username = "tester";
        service.createShow(username, createPayload("Arcane", "watching", 3, 9, 9.5));
        service.createShow(username, createPayload("The Batman", "completed", 0, 0, 8.0));

        Map<String, Object> stats = service.buildStatsResponse(username);
        Map<?, ?> byStatus = (Map<?, ?>) stats.get("byStatus");
        Map<?, ?> byType = (Map<?, ?>) stats.get("byType");

        assertEquals(2, stats.get("totalShows"));
        assertEquals(8.75, stats.get("averageRating"));
        assertEquals(1, byStatus.get("watching"));
        assertEquals(1, byStatus.get("completed"));
        assertEquals(2, byType.get("series"));
    }

    private static Map<String, Object> createPayload(String title, String status, int episodesWatched, int totalEpisodes, double rating) {
        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", title);
        payload.put("type", "series");
        payload.put("status", status);
        payload.put("description", "Animated fantasy drama.");
        payload.put("releaseDate", "2021");
        payload.put("image", "data:image/png;base64,abc");
        payload.put("episodesWatched", episodesWatched);
        payload.put("totalEpisodes", totalEpisodes);
        payload.put("rating", rating);
        payload.put("genres", java.util.List.of("Animation"));
        return payload;
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
}
