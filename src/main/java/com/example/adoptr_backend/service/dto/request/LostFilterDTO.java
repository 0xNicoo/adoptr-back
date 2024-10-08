package com.example.adoptr_backend.service.dto.request;

import com.example.adoptr_backend.model.AnimalType;
import com.example.adoptr_backend.model.LostStatusType;
import com.example.adoptr_backend.model.SexType;
import com.example.adoptr_backend.model.SizeType;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LostFilterDTO {
    private String title;
    private LocalDateTime creationDate;
    private SexType sexType;
    private AnimalType animalType;
    private SizeType sizeType;
    private LostStatusType lostStatusType;
    private int ageYears;
    private Long user_id;
    private double longitude;
    private double latitude;
    private Boolean wasFound = false;
}
