package com.example.adoptr_backend.service;

import com.example.adoptr_backend.model.Image;
import com.example.adoptr_backend.model.ImageType;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    Long uploadImage(MultipartFile file, ImageType type, Long modelId);

    String getS3url(Long modelId, ImageType image);

    void deleteImage(Long modelId, ImageType type);

    Long uploadServiceTypeImage(MultipartFile file, Long modelId);
}
