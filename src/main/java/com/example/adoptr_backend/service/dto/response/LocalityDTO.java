package com.example.adoptr_backend.service.dto.response;

import lombok.Data;

@Data
public class LocalityDTO {
    private Long id;
    private String name;
    private ProvinceDTO province;
}