package com.watchvault.watchvaultbackendspringboot.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class ShowResponse {
    private final Long id;
    private final String title;
    private final String type;
    private final String status;
    private final String description;
    private final LocalDate releaseDate;
    private final String image;
    private final Integer episodesWatched;
    private final Integer totalEpisodes;
    private final Double rating;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public ShowResponse(
            Long id,
            String title,
            String type,
            String status,
            String description,
            LocalDate releaseDate,
            String image,
            Integer episodesWatched,
            Integer totalEpisodes,
            Double rating,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.status = status;
        this.description = description;
        this.releaseDate = releaseDate;
        this.image = image;
        this.episodesWatched = episodesWatched;
        this.totalEpisodes = totalEpisodes;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public String getImage() {
        return image;
    }

    public Integer getEpisodesWatched() {
        return episodesWatched;
    }

    public Integer getTotalEpisodes() {
        return totalEpisodes;
    }

    public Double getRating() {
        return rating;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
