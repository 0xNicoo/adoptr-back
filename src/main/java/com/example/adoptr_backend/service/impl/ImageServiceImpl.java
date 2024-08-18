package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.model.Image;
import com.example.adoptr_backend.model.ImageType;
import com.example.adoptr_backend.repository.ImageRepository;
import com.example.adoptr_backend.service.ImageService;
import com.example.adoptr_backend.util.ImageUtil;
import com.example.adoptr_backend.util.S3Support;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Long uploadImage(MultipartFile file, ImageType type) {
        Image image = ImageUtil.createImage(file, type);
        image = imageRepository.save(image);
        S3Support.upload(ImageUtil.buildPath(image), file);
        return image.getId();
    }
}
