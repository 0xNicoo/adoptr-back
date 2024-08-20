package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository <Profile, Long>{
    boolean existsByUserId(Long userId);
}
