package com.example.adoptr_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private LocalDateTime createdAt;

    @Column
    private Long modelId;

    @Column
    private NotificationModelType modelType;

    @Column
    private String userName;

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }
}
