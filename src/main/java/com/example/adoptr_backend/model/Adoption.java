package com.example.adoptr_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Adoption extends Publication {

    @Enumerated(EnumType.STRING )
    private SexType sexType;

    @Column
    private boolean vaccinated;

    @Column
    private boolean unprotected;

    @Column
    private boolean castrated;

    @Enumerated(EnumType.STRING)
    private AnimalType animalType;

    @Enumerated(EnumType.STRING)
    private SizeType sizeType;

    @Enumerated(EnumType.STRING)
    private AdoptionStatusType adoptionStatusType;

    @Column
    private int ageYears;

    @Column
    private int ageMonths;

    @PrePersist
    public void prePersist(){
        this.adoptionStatusType = AdoptionStatusType.FOR_ADOPTION;
        super.setCreationDate(LocalDateTime.now());
    }
}
