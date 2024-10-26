package com.example.adoptr_backend.service.dto.response;

import com.example.adoptr_backend.model.PublicationType;
import lombok.Data;

@Data
public class PublicationDTO {
    private Long id;
    private String title;
    private PublicationType type;
    private UserDTO user;
}
