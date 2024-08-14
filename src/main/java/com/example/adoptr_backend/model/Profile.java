package com.example.adoptr_backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private Boolean gender;

    @Column
    private String description;

    // TODO AGREGAR LA IMAGEN

    @Column
    private Long createdByUser;

}
