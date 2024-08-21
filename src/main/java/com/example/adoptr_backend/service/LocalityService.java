package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.LocalityDTOin;
import com.example.adoptr_backend.service.dto.request.LocalityFilterDTO;
import com.example.adoptr_backend.service.dto.response.LocalityDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LocalityService {
    LocalityDTO create(LocalityDTOin dto);

    Page<LocalityDTO> getAll(LocalityFilterDTO filterDTO, Pageable pageable);
}
