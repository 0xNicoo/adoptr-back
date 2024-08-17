package com.example.adoptr_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.awt.*;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String description;

    // TODO VER EL TEMA DE LA IMAGEN
    @Column
    private Image photo;

    @Column
    private LocalDate creationDate;
}
