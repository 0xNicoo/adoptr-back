package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.AdoptionDTOin;
import com.example.adoptr_backend.service.dto.request.AdoptionFilterDTO;
import com.example.adoptr_backend.service.dto.request.AdoptionStatusDTOin;
import com.example.adoptr_backend.service.dto.response.AdoptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdoptionService {

    AdoptionDTO create(AdoptionDTOin dto);

    AdoptionDTO getById(Long id);

    Page<AdoptionDTO> getAll(AdoptionFilterDTO filter, Pageable pageable);

    AdoptionDTO update(Long id, AdoptionDTOin dto);

    void delete(Long id);

    void changeStatus(AdoptionStatusDTOin dtoIn);
}
