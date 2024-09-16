package com.example.adoptr_backend.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Service extends Publication {

    @Column
    private String street;

    @Column
    private int number;

    @ManyToOne
    @JoinColumn(name = "serviceType_id")
    private ServiceType serviceType;

    @PrePersist
    public void prePersist(){
        super.setCreationDate(LocalDateTime.now());
    }
}
