package com.watchvault.watchvaultbackendspringboot.service;

import com.watchvault.watchvaultbackendspringboot.entity.ShowEntity;
import com.watchvault.watchvaultbackendspringboot.entity.UserEntity;
import com.watchvault.watchvaultbackendspringboot.error.NotFoundException;
import com.watchvault.watchvaultbackendspringboot.repository.ShowRepository;
import com.watchvault.watchvaultbackendspringboot.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {
    private static final double AVERAGE_EPISODE_MINUTES = 25.0;

    private final UserRepository userRepository;
    private final ShowRepository showRepository;

    public StatsService(UserRepository userRepository, ShowRepository showRepository) {
        this.userRepository = userRepository;
        this.showRepository = showRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStats(String username) {
        UserEntity user = userRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new NotFoundException("User not found."));

        List<ShowEntity> shows = showRepository.findByUserId(user.getId(), Pageable.unpaged()).getContent();

        long totalShows = shows.size();
        long episodesWatched = shows.stream()
                .mapToLong(this::watchedEpisodesForStats)
                .sum();
        double daysWatched = Math.round((episodesWatched * AVERAGE_EPISODE_MINUTES / 1440.0) * 10.0) / 10.0;

        Double averageRating = shows.stream()
                .map(ShowEntity::getRating)
                .filter(rating -> rating != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(Double.NaN);

        if (Double.isNaN(averageRating)) {
            averageRating = null;
        }

        Map<String, Long> byStatus = new LinkedHashMap<>();
        Map<String, Long> byType = new LinkedHashMap<>();

        for (ShowEntity show : shows) {
            byStatus.merge(show.getStatus(), 1L, Long::sum);
            byType.merge(show.getType(), 1L, Long::sum);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalShows", totalShows);
        response.put("averageRating", averageRating);
        response.put("episodesWatched", episodesWatched);
        response.put("daysWatched", daysWatched);
        response.put("byStatus", byStatus);
        response.put("byType", byType);

        return response;
    }

    private long watchedEpisodesForStats(ShowEntity show) {
        Integer episodesWatched = show.getEpisodesWatched();

        if (episodesWatched != null && episodesWatched > 0) {
            return episodesWatched;
        }

        Integer totalEpisodes = show.getTotalEpisodes();

        if (totalEpisodes != null && totalEpisodes > 0) {
            return totalEpisodes;
        }

        return 0;
    }
}
