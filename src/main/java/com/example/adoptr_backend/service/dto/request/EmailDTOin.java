package com.example.adoptr_backend.service.dto.request;

import lombok.Data;

@Data
public class EmailDTOin {
    private String to;
    private String subject;
    private String message;
}
