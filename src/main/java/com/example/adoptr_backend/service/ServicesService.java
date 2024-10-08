package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.AdoptionDTOin;
import com.example.adoptr_backend.service.dto.request.ServiceDTOin;
import com.example.adoptr_backend.service.dto.request.ServiceFilterDTO;
import com.example.adoptr_backend.service.dto.response.AdoptionDTO;
import com.example.adoptr_backend.service.dto.response.ServiceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServicesService {
    ServiceDTO create(ServiceDTOin dto);

    Page<ServiceDTO> getAll(ServiceFilterDTO filter, Pageable pageable);

    ServiceDTO getById(Long id);

    void delete(Long id);

    ServiceDTO update(Long id, ServiceDTOin dto);

    Page<ServiceDTO> getByUserId(Long userId, Pageable pageable);
}
