package com.example.adoptr_backend.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Service extends Publication {

    @Column
    private String street;

    @Column
    private int number;

    @ManyToOne
    @JoinColumn(name = "servicetype_id")
    private ServiceType servicetype;

}
