package com.example.adoptr_backend.service.dto.request;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class DonationFilterDTO {
    private Double amount;
    private String description;
    private String status;
    private String paymentMethod;
    private OffsetDateTime approvalDate;
}
