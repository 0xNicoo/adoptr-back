package com.example.adoptr_backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Locality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;
}
