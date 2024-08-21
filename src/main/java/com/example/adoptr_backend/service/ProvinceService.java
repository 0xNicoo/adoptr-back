package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.ProvinceDTOin;
import com.example.adoptr_backend.service.dto.response.ProvinceDTO;

public interface ProvinceService {
    ProvinceDTO create(ProvinceDTOin dto);
}
