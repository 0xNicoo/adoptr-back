package com.example.adoptr_backend.service;


import com.example.adoptr_backend.service.dto.request.ServiceTypeDTOin;
import com.example.adoptr_backend.service.dto.request.ServiceTypeFilterDTO;
import com.example.adoptr_backend.service.dto.response.ServiceTypeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceTypeService {

    ServiceTypeDTO create(ServiceTypeDTOin dto);

    ServiceTypeDTO getById(Long id);

    Page<ServiceTypeDTO> getAll(ServiceTypeFilterDTO filterDTO, Pageable pageable);

    ServiceTypeDTO update(Long id, ServiceTypeDTOin dto);
}
