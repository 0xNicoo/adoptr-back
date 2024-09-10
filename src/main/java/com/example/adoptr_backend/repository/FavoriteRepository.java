package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByAdoptionIdAndUserId(Long adoptionId, Long userId);

    Page<Favorite> findByUserId(Long userId, Pageable pageable);
}
