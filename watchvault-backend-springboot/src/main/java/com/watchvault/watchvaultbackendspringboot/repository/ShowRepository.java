package com.watchvault.watchvaultbackendspringboot.repository;

import com.watchvault.watchvaultbackendspringboot.entity.ShowEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShowRepository extends JpaRepository<ShowEntity, Long> {
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
