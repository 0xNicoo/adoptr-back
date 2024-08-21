package com.example.adoptr_backend.service.dto.request;

import com.example.adoptr_backend.service.dto.response.ProvinceDTO;
import lombok.Data;

@Data
public class LocalityFilterDTO {
    private String name;
    private Long provinceId;
}
