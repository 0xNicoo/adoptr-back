package com.example.adoptr_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Lost extends Publication{

    @Enumerated(EnumType.STRING )
    private SexType sexType;

    @Enumerated(EnumType.STRING)
    private AnimalType animalType;

    @Enumerated(EnumType.STRING)
    private SizeType sizeType;

    @Enumerated(EnumType.STRING)
    private LostStatusType lostStatusType;

    @Column
    private int ageYears;

    @Column
    private int ageMonths;

    @Column
    private double longitude;

    @Column
    private double latitude;

    @PrePersist
    public void prePersist(){
        this.lostStatusType = LostStatusType.LOST;
        super.setCreationDate(LocalDateTime.now());
    }
}
