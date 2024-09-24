package com.example.adoptr_backend.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Data
public class ServiceDTOin {
    private String title;
    private String description;
    private String street;
    private int number;
    private MultipartFile image;
    private Long locality_id;
    private Long serviceType_id;
}
