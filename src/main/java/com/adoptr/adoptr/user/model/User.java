package com.adoptr.adoptr.user.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String provider;
    private String providerUserId;
    private String email;
    private String name;
    private LocalDateTime createdAt;
}
