package com.example.adoptr_backend.service.dto.response;

import com.example.adoptr_backend.model.NotificationModelType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private UserDTO user;
    private LocalDateTime createdAt;
    private Long modelId;
    private NotificationModelType modelType;
}
