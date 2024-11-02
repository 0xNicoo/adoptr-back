package com.example.adoptr_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Data
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private OffsetDateTime approvalDate;

}
