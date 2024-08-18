package com.example.adoptr_backend.service;

import com.example.adoptr_backend.model.Image;
import com.example.adoptr_backend.model.ImageType;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    Long uploadImage(MultipartFile file, ImageType type);
}
