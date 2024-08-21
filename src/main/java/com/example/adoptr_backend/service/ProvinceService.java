package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.ProvinceDTOin;
import com.example.adoptr_backend.service.dto.request.ProvinceFilterDTO;
import com.example.adoptr_backend.service.dto.response.ExampleDTO;
import com.example.adoptr_backend.service.dto.response.ProvinceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProvinceService {
    ProvinceDTO create(ProvinceDTOin dto);

    Page<ProvinceDTO> getAll(ProvinceFilterDTO filterDTO, Pageable pageable);
}
