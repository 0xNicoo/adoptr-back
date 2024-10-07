package com.example.adoptr_backend.service.dto.response;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ServiceTypeDTO {
    private Long id;
    private String name;
    private String description;
    private String s3Url;

}
