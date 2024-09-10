package com.example.adoptr_backend.service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteDTO {

    private Long id;

    private LocalDateTime createdAt;

    private Long userId;

    private Long adoptionId;
}
