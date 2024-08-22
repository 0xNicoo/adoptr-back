package com.example.adoptr_backend.service.dto.response;

import com.example.adoptr_backend.model.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdoptionDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime creationDate;
    private SexType sexType;
    private boolean vaccinated;
    private boolean unprotected;
    private boolean castrated;
    private AnimalType animalType;
    private SizeType sizeType;
    private AdoptionStatusType adoptionStatusType;
    private int ageYears;
    private int ageMonths;
    private UserDTO user;
    private String s3Url;
    private LocalityDTO locality;
}
