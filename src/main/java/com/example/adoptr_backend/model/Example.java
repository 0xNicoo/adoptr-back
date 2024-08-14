package com.example.adoptr_backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Example {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String text;

    @Enumerated(EnumType.STRING)
    private ExampleType type;

    @Column
    private Long createdByUser;

    @Column
    private Boolean active;

}
