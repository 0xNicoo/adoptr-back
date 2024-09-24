package com.example.adoptr_backend.service.dto.request;

import com.example.adoptr_backend.model.AdoptionStatusType;
import com.example.adoptr_backend.model.AnimalType;
import com.example.adoptr_backend.model.SexType;
import com.example.adoptr_backend.model.SizeType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdoptionFilterDTO {
    private SexType sexType;
    private boolean vaccinated;
    private boolean unprotected;
    private boolean castrated;
    private String title;
    private AnimalType animalType;
    private SizeType sizeType;
    private int ageYears;
    private AdoptionStatusType adoptionStatusType;
    private LocalDate creationDate;
    private Long locality_id;
    private Long user_id;
    private Long province_id;
}
