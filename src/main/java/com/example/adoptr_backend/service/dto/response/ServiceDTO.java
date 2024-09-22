package com.example.adoptr_backend.service.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ServiceDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime creationDate;
    private String Street;
    private int number;
    private UserDTO user;
    private String s3Url;
    private LocalityDTO locality;
    private ServiceTypeDTO serviceType;
}
