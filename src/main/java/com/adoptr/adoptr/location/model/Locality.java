package com.adoptr.adoptr.location.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Locality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;
}
