package com.watchvault.watchvaultbackendspringboot.repository;

import com.watchvault.watchvaultbackendspringboot.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByNameIgnoreCase(String name);
}
