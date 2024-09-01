package com.example.adoptr_backend.service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatDTO {
    private Long id;
    private Long publicationId;
    private LocalDateTime createdAt;
    private Long adopterUserId;
    private Long publicationUserId;
    private List<MessageDTO> messages;
}
