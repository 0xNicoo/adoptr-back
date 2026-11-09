package com.adoptr.adoptr.shared.storage;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Datos comunes de una imagen guardada: el archivo está en el almacenamiento, acá solo su referencia. */
@MappedSuperclass
@Getter
@Setter
public abstract class StoredImage {

    @Column(name = "s3_key", nullable = false, unique = true)
    private String s3Key;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public void fillFrom(String key, ProcessedImage image, LocalDateTime now) {
        this.s3Key = key;
        this.contentType = image.contentType();
        this.sizeBytes = image.bytes().length;
        this.createdAt = now;
    }
}
