package com.example.adoptr_backend.service.impl;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.example.adoptr_backend.model.Image;
import com.example.adoptr_backend.model.ImageType;
import com.example.adoptr_backend.repository.ImageRepository;
import com.example.adoptr_backend.service.ImageService;
import com.example.adoptr_backend.util.AuthSupport;
import com.example.adoptr_backend.util.ImageUtil;
import com.example.adoptr_backend.util.S3Support;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class ImageServiceImpl implements ImageService {

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    private final ImageRepository imageRepository;

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Long uploadImage(MultipartFile file, ImageType type, Long modelId) {
        Image image = ImageUtil.createImage(file, type, modelId);
        image = imageRepository.save(image);
        S3Support.upload(ImageUtil.buildPath(image), file);
        return image.getId();
    }

    @Override
    public String getS3url(Long modelId, ImageType type) {
        List<Image> imageList = imageRepository.findByModelIdAndType(modelId, type);
        /**
         * ahora agarra la primera
         * pero esta preparado para devolver muchas,
         * para el caso de una publicacion con muchas fotos
         */
        Image image = imageList.get(0);
        String imageUrl = S3Support.getS3url(image);
        return imageUrl;
    }


}
