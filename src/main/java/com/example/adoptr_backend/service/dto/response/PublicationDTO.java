package com.example.adoptr_backend.service.dto.response;

import lombok.Data;

@Data
public class PublicationDTO {
    private Long id;
    private String title;
    private UserDTO user;
}
