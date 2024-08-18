package com.example.adoptr_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalName;

    @Column(nullable = false)
    private UUID s3name;

    private long size;

    private String extension;

    @Enumerated(EnumType.STRING)
    private ImageType type;

    private LocalDateTime createdAt;

    private Long userId;

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }
}
