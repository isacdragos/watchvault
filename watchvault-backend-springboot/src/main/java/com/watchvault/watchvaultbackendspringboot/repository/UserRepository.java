package com.watchvault.watchvaultbackendspringboot.repository;

import com.watchvault.watchvaultbackendspringboot.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    Optional<UserEntity> findFirstByOrderByIdAsc();
}
