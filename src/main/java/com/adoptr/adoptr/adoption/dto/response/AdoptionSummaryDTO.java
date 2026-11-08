package com.adoptr.adoptr.adoption.dto.response;

import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.model.LifeStage;
import com.adoptr.adoptr.adoption.model.PetSex;
import com.adoptr.adoptr.adoption.model.PetSize;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.location.dto.response.LocalityDTO;
import com.adoptr.adoptr.location.dto.response.ProvinceDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.YearMonth;

/** Tarjeta de catálogo y de «mis publicaciones»: la ficha sin descripción ni responsable. */
@Getter
@Setter
public class AdoptionSummaryDTO {
    private Long id;
    private String petName;
    private Species species;
    private PetSex sex;
    private PetSize size;
    @JsonFormat(pattern = "yyyy-MM")
    private YearMonth estimatedBirthDate;
    private LifeStage lifeStage;
    private boolean vaccinated;
    private boolean dewormed;
    private boolean neutered;
    private AdoptionStatus status;
    private LocalityDTO locality;
    private ProvinceDTO province;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
