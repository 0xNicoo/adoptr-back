package com.adoptr.adoptr.adoption.model;

import com.adoptr.adoptr.publication.model.Publication;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Ficha de una mascota en adopción. Las reglas (tamaño según especie, fechas, estado) las aplica el servicio. */
@Entity
@Table(name = "adoption_publication")
@Getter
@Setter
public class AdoptionPublication extends Publication {

    @Column(length = 50, nullable = false)
    private String petName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Species species;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetSex sex;

    /** Solo para perros; {@code null} para gatos. */
    @Enumerated(EnumType.STRING)
    private PetSize size;

    /** Mes y año estimados, guardados como el día 1 de ese mes. */
    @Column(nullable = false)
    private LocalDate estimatedBirthDate;

    @Column(nullable = false)
    private boolean vaccinated;

    @Column(nullable = false)
    private boolean dewormed;

    @Column(nullable = false)
    private boolean neutered;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdoptionStatus status;
}
