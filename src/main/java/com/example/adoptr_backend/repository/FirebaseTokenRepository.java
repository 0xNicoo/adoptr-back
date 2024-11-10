package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.FirebaseToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FirebaseTokenRepository extends JpaRepository<FirebaseToken, Long> {
    Optional<FirebaseToken> findByUserId(@Param("userId") Long userId);
}
