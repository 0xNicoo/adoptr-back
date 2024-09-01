package com.example.adoptr_backend.service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long id;
    private Long chatId;
    private String content;
    private Long userId;
    private LocalDateTime createdAt;
}
