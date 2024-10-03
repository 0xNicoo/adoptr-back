package com.example.adoptr_backend.service.dto.response;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDTO {
    private Long id;
    private String description;
    private LocalDateTime date;
    private UserDTO user;
    private String s3Url;
}
