package com.example.adoptr_backend.service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatDTO {
    private Long id;
    private LocalDateTime createdAt;
    private Long adopterUserId;
    private String adopterUserName;
    private Long publicationUserId;
    private String publicationUserName;
    private List<MessageDTO> messages;
    private PublicationChatDTO publication;
}
