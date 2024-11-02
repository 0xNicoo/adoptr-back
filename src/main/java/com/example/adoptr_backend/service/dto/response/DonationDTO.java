package com.example.adoptr_backend.service.dto.response;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class DonationDTO {
    private Long id;
    private Double amount;
    private String description;
    private String status;
    private String paymentMethod;
    private OffsetDateTime approvalDate;
}
