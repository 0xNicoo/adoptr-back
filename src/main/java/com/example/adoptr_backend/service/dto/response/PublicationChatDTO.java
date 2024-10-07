package com.example.adoptr_backend.service.dto.response;

import com.example.adoptr_backend.model.PublicationType;
import lombok.Data;

import java.util.List;

@Data
public class PublicationChatDTO {
    private Long id;
    private String title;
    private PublicationType type;
    private String description;
    private UserDTO user;
    private String s3Url;
}
