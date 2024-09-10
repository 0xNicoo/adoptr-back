package com.example.adoptr_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDateTime createdAt;

    @Column
    private Long userId;

    @Column
    private Long adoptionId;

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }
}
