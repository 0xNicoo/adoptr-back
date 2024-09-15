package com.example.adoptr_backend.service.dto.request;

import com.example.adoptr_backend.model.AnimalType;
import com.example.adoptr_backend.model.LostStatusType;
import com.example.adoptr_backend.model.SexType;
import com.example.adoptr_backend.model.SizeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Data
public class LostDTOin {
    private String title;
    private String description;
    private SexType sexType;
    private AnimalType animalType;
    private SizeType sizeType;
    private int ageYears;
    private int ageMonths;
    private MultipartFile image;
}
