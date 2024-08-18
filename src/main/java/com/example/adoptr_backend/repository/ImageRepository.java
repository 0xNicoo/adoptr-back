package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Image;
import com.example.adoptr_backend.model.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByModelIdAndType(@Param("modelId") Long modelId, @Param("type")ImageType imageType);
}
