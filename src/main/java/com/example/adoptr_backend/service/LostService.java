package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.LostDTOin;
import com.example.adoptr_backend.service.dto.request.LostFilterDTO;
import com.example.adoptr_backend.service.dto.request.LostStatusDTOin;
import com.example.adoptr_backend.service.dto.response.LostDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface LostService {

    LostDTO create(LostDTOin dto);

    LostDTO getById(Long id);

    Page<LostDTO> getAll(LostFilterDTO filter, Pageable pageable);

    LostDTO update(Long id, LostDTOin dto);

    void delete(Long id);

    void changeStatus(LostStatusDTOin dtoIn);

    Long getLostCount();
}
