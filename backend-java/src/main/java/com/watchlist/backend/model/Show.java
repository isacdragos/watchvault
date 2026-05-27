package com.watchlist.backend.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record Show(
        String id,
        String title,
        String type,
        String status,
        String description,
        String releaseDate,
        String image,
        Integer episodesWatched,
        Integer totalEpisodes,
        Double rating,
        List<String> genres,
        String createdAt,
        String updatedAt
) {
    public Map<String, Object> toMap() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("type", type);
        result.put("status", status);
        result.put("description", description);
        result.put("releaseDate", releaseDate);
        result.put("image", image);
        result.put("episodesWatched", episodesWatched);
        result.put("totalEpisodes", totalEpisodes);
        result.put("rating", rating);
        result.put("genres", genres == null ? null : new ArrayList<>(genres));
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        return result;
    }
}
