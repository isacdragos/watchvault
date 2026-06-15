package com.watchvault.watchvaultbackendspringboot.repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface ShowListView {
    Long getId();

    String getTitle();

    String getType();

    String getStatus();

    LocalDate getReleaseDate();

    Integer getEpisodesWatched();

    Integer getTotalEpisodes();

    Double getRating();

    OffsetDateTime getCreatedAt();

    OffsetDateTime getUpdatedAt();
}
