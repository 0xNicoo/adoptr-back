package com.example.adoptr_backend.service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FirebaseTokenDTO {

    private long id;

    private String token;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
