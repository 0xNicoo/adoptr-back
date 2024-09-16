package com.example.adoptr_backend.service.dto.request;

import lombok.Data;

import java.time.LocalDate;
@Data
public class ServiceFilterDTO {
    private String tittle;
    private String street;
    private int number;
    private LocalDate creationDate;
    private Long locality_id;
    private Long user_id;
    private Long serviceType_id;
}
