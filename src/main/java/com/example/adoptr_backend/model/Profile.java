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

    @Enumerated(EnumType.STRING)
    private GenderType genderType;

    @Column
    private String description;

    @Column
    private Long imageId;

    @OneToOne
    private User user;

    @ManyToOne
    @JoinColumn(name = "locality_id")
    private Locality locality;

}
