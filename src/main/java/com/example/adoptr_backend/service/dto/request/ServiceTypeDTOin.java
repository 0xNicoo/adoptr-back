package com.example.adoptr_backend.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Data
public class ServiceTypeDTOin {
    private String name;
    private String description;
    private MultipartFile image;

}
