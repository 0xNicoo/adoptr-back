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

    @Column
    private Long imageId;

    @OneToOne
    private User user;

    //TODO: agregar la localidad y la provincia

}
