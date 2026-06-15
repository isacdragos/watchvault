package com.watchvault.watchvaultbackendspringboot.repository;

import com.watchvault.watchvaultbackendspringboot.entity.ShowEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShowRepository extends JpaRepository<ShowEntity, Long> {
    @Query(
            value = """
                    select
                        show.id as id,
                        show.title as title,
                        show.type as type,
                        show.status as status,
                        show.releaseDate as releaseDate,
                        show.episodesWatched as episodesWatched,
                        show.totalEpisodes as totalEpisodes,
                        show.rating as rating,
                        show.createdAt as createdAt,
                        show.updatedAt as updatedAt
                    from ShowEntity show
                    where show.user.id = :userId
                    """,
            countQuery = "select count(show) from ShowEntity show where show.user.id = :userId"
    )
    Page<ShowListView> findListByUserId(@Param("userId") Long userId, Pageable pageable);

    Page<ShowEntity> findByUserId(Long userId, Pageable pageable);

    Page<ShowEntity> findByUserIdAndStatusIgnoreCase(Long userId, String status, Pageable pageable);

    Page<ShowEntity> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title, Pageable pageable);

    Page<ShowEntity> findByUserIdAndStatusIgnoreCaseAndTitleContainingIgnoreCase(
            Long userId,
            String status,
            String title,
            Pageable pageable
    );

    Optional<ShowEntity> findByIdAndUserId(Long id, Long userId);

    void deleteAllByUserId(Long userId);
}
