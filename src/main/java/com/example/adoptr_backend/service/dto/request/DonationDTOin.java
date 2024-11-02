package com.example.adoptr_backend.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@AllArgsConstructor
@Data
public class DonationDTOin {
    private Double amount;
    private String description;
    private String status;
    private String paymentMethod;
    private OffsetDateTime approvalDate;
}
