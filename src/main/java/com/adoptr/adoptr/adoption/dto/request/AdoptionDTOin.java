package com.adoptr.adoptr.adoption.dto.request;

import com.adoptr.adoptr.adoption.model.PetSex;
import com.adoptr.adoptr.adoption.model.PetSize;
import com.adoptr.adoptr.adoption.model.Species;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.YearMonth;

/**
 * Body de alta y edición. Las reglas que cruzan campos (tamaño según especie, rango de la fecha,
 * localidad existente) las valida el servicio.
 */
@Data
public class AdoptionDTOin {

    @NotBlank
    @Size(max = 50)
    private String petName;

    @NotBlank
    @Size(max = 2000)
    private String description;

    @NotNull
    private Species species;

    @NotNull
    private PetSex sex;

    /** Obligatorio para perros; debe faltar para gatos. */
    private PetSize size;

    /** Mes y año estimados, {@code "aaaa-mm"}. */
    @NotNull
    @JsonFormat(pattern = "yyyy-MM")
    private YearMonth estimatedBirthDate;

    /** Sí o no: si no se sabe, se carga {@code false}. */
    @NotNull
    private Boolean vaccinated;

    @NotNull
    private Boolean dewormed;

    @NotNull
    private Boolean neutered;

    @NotNull
    private Long localityId;
}
