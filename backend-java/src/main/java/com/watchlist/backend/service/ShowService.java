package com.watchlist.backend.service;

import com.watchlist.backend.errors.ApiException;
import com.watchlist.backend.model.Show;
import com.watchlist.backend.repository.InMemoryShowRepository;
import com.watchlist.backend.validation.ShowValidator.PaginationQuery;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ShowService {
    private final InMemoryShowRepository repository;

    public ShowService(InMemoryShowRepository repository) {
        this.repository = repository;
    }

    public Map<String, Object> listShows(String username, PaginationQuery query) {
        List<Show> allShows = repository.findAll(username);
        List<Show> filteredShows = filterShows(allShows, query);

        int totalItems = filteredShows.size();
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / query.limit());
        int startIndex = (query.page() - 1) * query.limit();
        List<Map<String, Object>> items = filteredShows.stream()
                .skip(startIndex)
                .limit(query.limit())
                .map(Show::toMap)
                .toList();

        LinkedHashMap<String, Object> pagination = new LinkedHashMap<>();
        pagination.put("page", query.page());
        pagination.put("limit", query.limit());
        pagination.put("totalItems", totalItems);
        pagination.put("totalPages", totalPages);
        pagination.put("hasNextPage", query.page() < totalPages);
        pagination.put("hasPreviousPage", query.page() > 1 && totalItems > 0);

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("items", items);
        response.put("pagination", pagination);
        return response;
    }

    public Map<String, Object> getShowById(String username, String id) {
        return requireShow(username, id).toMap();
    }

    public Map<String, Object> createShow(String username, Map<String, Object> payload) {
        String timestamp = Instant.now().toString();
        Show created = repository.insert(username, toShow(null, payload, timestamp, timestamp));
        return created.toMap();
    }

    public Map<String, Object> replaceShow(String username, String id, Map<String, Object> payload) {
        Show existing = requireShow(username, id);
        Show replaced = repository.replace(username, id, toShow(id, payload, existing.createdAt(), Instant.now().toString()));
        return Objects.requireNonNull(replaced).toMap();
    }

    public Map<String, Object> updateShow(String username, String id, Map<String, Object> payload) {
        Show existing = requireShow(username, id);
        LinkedHashMap<String, Object> merged = mergeExistingShowWithChanges(existing, payload);

        Show updated = repository.replace(
                username,
                id,
                toShow(id, merged, existing.createdAt(), Instant.now().toString())
        );

        return Objects.requireNonNull(updated).toMap();
    }

    public void deleteShow(String username, String id) {
        if (!repository.delete(username, id)) {
            throw new ApiException(404, "Show not found.");
        }
    }

    public Map<String, Object> buildStatsResponse(String username) {
        List<Show> shows = repository.findAll(username);
        LinkedHashMap<String, Integer> byStatus = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> byType = new LinkedHashMap<>();
        List<Double> ratings = new ArrayList<>();

        for (Show show : shows) {
            byStatus.merge(show.status(), 1, Integer::sum);
            byType.merge(show.type(), 1, Integer::sum);

            if (show.rating() != null) {
                ratings.add(show.rating());
            }
        }

        Double averageRating = null;

        if (!ratings.isEmpty()) {
            double sum = ratings.stream().mapToDouble(Double::doubleValue).sum();
            averageRating = Math.round((sum / ratings.size()) * 100.0) / 100.0;
        }

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("totalShows", shows.size());
        response.put("averageRating", averageRating);
        response.put("byStatus", byStatus);
        response.put("byType", byType);
        return response;
    }

    private List<Show> filterShows(List<Show> allShows, PaginationQuery query) {
        return allShows.stream()
                .filter(show -> matchesStatusFilter(show, query.status()))
                .filter(show -> matchesSearchFilter(show, query.search()))
                .toList();
    }

    private boolean matchesStatusFilter(Show show, String statusFilter) {
        return statusFilter == null || statusFilter.equals(show.status());
    }

    private boolean matchesSearchFilter(Show show, String searchFilter) {
        return searchFilter == null
                || show.title().toLowerCase().contains(searchFilter.toLowerCase());
    }

    private LinkedHashMap<String, Object> mergeExistingShowWithChanges(Show existing, Map<String, Object> changes) {
        // PATCH keeps old fields unless the client explicitly sends a replacement.
        LinkedHashMap<String, Object> merged = new LinkedHashMap<>(existing.toMap());

        changes.forEach((key, value) -> {
            if (!"id".equals(key) && !"createdAt".equals(key) && !"updatedAt".equals(key)) {
                merged.put(key, value);
            }
        });

        return merged;
    }

    public void seedUserShows(String username) {
        if (!repository.findAll(username).isEmpty()) {
            return;
        }

        for (Show seededShow : com.watchlist.backend.SeedData.createShows()) {
            repository.insert(username, seededShow);
        }
    }

    private Show requireShow(String username, String id) {
        Show show = repository.findById(username, id);

        if (show == null) {
            throw new ApiException(404, "Show not found.");
        }

        return show;
    }

    @SuppressWarnings("unchecked")
    private Show toShow(String id, Map<String, Object> payload, String createdAt, String updatedAt) {
        return new Show(
                id,
                (String) payload.get("title"),
                (String) payload.get("type"),
                (String) payload.get("status"),
                (String) payload.get("description"),
                (String) payload.get("releaseDate"),
                (String) payload.get("image"),
                (Integer) payload.get("episodesWatched"),
                (Integer) payload.get("totalEpisodes"),
                (Double) payload.get("rating"),
                payload.get("genres") == null ? null : new ArrayList<>((List<String>) payload.get("genres")),
                createdAt,
                updatedAt
        );
    }
}
