package com.example.adoptr_backend.service.dto.response;

import lombok.Data;

@Data
public class PublicationChatDTO {
    private Long id;
    private String title;
    private UserDTO user;
}
