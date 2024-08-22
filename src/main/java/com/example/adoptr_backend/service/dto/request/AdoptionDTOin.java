package com.example.adoptr_backend.service.dto.request;

import com.example.adoptr_backend.model.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AdoptionDTOin {
    private String title;
    private String description;
    private SexType sexType;
    private boolean vaccinated;
    private boolean unprotected;
    private boolean castrated;
    private AnimalType animalType;
    private SizeType sizeType;
    private AdoptionStatusType adoptionStatusType;
    private int ageYears;
    private int ageMonths;
    private MultipartFile image;
    private Long locality_id;
}
