package com.example.adoptr_backend.service.dto.response;

import com.example.adoptr_backend.model.AnimalType;
import com.example.adoptr_backend.model.LostStatusType;
import com.example.adoptr_backend.model.SexType;
import com.example.adoptr_backend.model.SizeType;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LostDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime creationDate;
    private SexType sexType;
    private AnimalType animalType;
    private SizeType sizeType;
    private LostStatusType lostStatusType;
    private int ageYears;
    private int ageMonths;
    private double longitude;
    private double latitude;
    private UserDTO user;
    private String s3Url;
    private LocalityDTO locality;
    private Double distanceWithoutUnit;
    private String distance;
}
