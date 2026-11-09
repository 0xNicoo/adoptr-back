package com.adoptr.adoptr.shared.storage;

import com.adoptr.adoptr.shared.exception.custom.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.images.storage", havingValue = "s3", matchIfMissing = true)
public class S3ImageStorage implements ImageStorage {

    /** Cada archivo tiene una key única y no cambia nunca: se puede cachear sin vencimiento práctico. */
    private static final String CACHE_CONTROL = "public, max-age=31536000, immutable";

    private final S3Client s3Client;
    private final String bucket;
    private final String publicBaseUrl;

    public S3ImageStorage(S3Client s3Client,
                          @Value("${aws.s3.bucket}") String bucket,
                          @Value("${aws.region}") String region,
                          @Value("${aws.s3.public-base-url}") String publicBaseUrl) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl.isBlank()
                ? "https://" + bucket + ".s3." + region + ".amazonaws.com"
                : publicBaseUrl.replaceAll("/+$", "");
    }

    @Override
    public void upload(String key, ProcessedImage image) {
        if (bucket.isBlank()) {
            log.error("Falta configurar AWS_S3_BUCKET: no se puede guardar {}", key);
            throw new ServiceUnavailableException(ImageError.STORAGE_UNAVAILABLE);
        }
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(image.contentType())
                            .cacheControl(CACHE_CONTROL)
                            .build(),
                    RequestBody.fromBytes(image.bytes()));
        } catch (SdkException e) {
            log.error("No se pudo subir {} a S3", key, e);
            throw new ServiceUnavailableException(ImageError.STORAGE_UNAVAILABLE);
        }
    }

    @Override
    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (SdkException e) {
            log.error("No se pudo borrar {} de S3: queda huérfano", key, e);
        }
    }

    @Override
    public String publicUrl(String key) {
        return publicBaseUrl + "/" + key;
    }
}
