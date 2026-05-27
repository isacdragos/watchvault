package com.watchlist.backend.repository;

import com.watchlist.backend.model.Show;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class InMemoryShowRepository {
    private final Map<String, Map<String, Show>> showsByUsername = new HashMap<>();
    private final Map<String, AtomicInteger> nextIdByUsername = new HashMap<>();

    public synchronized List<Show> findAll(String username) {
        return new ArrayList<>(getUserShows(username).values());
    }

    public synchronized Show findById(String username, String id) {
        return getUserShows(username).get(id);
    }

    public synchronized Show insert(String username, Show show) {
        String id = String.valueOf(getNextIdCounter(username).getAndIncrement());
        Show record = new Show(
                id,
                show.title(),
                show.type(),
                show.status(),
                show.description(),
                show.releaseDate(),
                show.image(),
                show.episodesWatched(),
                show.totalEpisodes(),
                show.rating(),
                show.genres(),
                show.createdAt(),
                show.updatedAt()
        );
        getUserShows(username).put(id, record);
        return record;
    }

    public synchronized Show replace(String username, String id, Show show) {
        Map<String, Show> userShows = getUserShows(username);

        if (!userShows.containsKey(id)) {
            return null;
        }

        Show record = new Show(
                id,
                show.title(),
                show.type(),
                show.status(),
                show.description(),
                show.releaseDate(),
                show.image(),
                show.episodesWatched(),
                show.totalEpisodes(),
                show.rating(),
                show.genres(),
                show.createdAt(),
                show.updatedAt()
        );
        userShows.put(id, record);
        return record;
    }

    public synchronized boolean delete(String username, String id) {
        return getUserShows(username).remove(id) != null;
    }

    public synchronized void reset(String username) {
        getUserShows(username).clear();
        getNextIdCounter(username).set(1);
    }

    private Map<String, Show> getUserShows(String username) {
        return showsByUsername.computeIfAbsent(username, ignored -> new LinkedHashMap<>());
    }

    private AtomicInteger getNextIdCounter(String username) {
        return nextIdByUsername.computeIfAbsent(username, ignored -> new AtomicInteger(1));
    }
}
