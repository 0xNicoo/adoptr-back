package com.example.adoptr_backend.service.dto.request;

import com.example.adoptr_backend.model.GenderType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileDTOin {
    private String firstName;
    private String lastName;
    private GenderType genderType;
    private String description;
    private MultipartFile image;
}
