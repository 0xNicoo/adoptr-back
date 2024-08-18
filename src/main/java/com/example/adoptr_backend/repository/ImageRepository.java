package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
