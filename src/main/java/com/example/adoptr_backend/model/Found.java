package com.example.adoptr_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Found {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
        private User user;

    @OneToOne
    @JoinColumn(name = "lost_id", nullable = false)
        private Lost lost;
}
