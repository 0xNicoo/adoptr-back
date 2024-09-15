package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.model.ServiceType;
import com.example.adoptr_backend.repository.ServiceTypeRepository;
import com.example.adoptr_backend.repository.specification.ServiceTypeSpec;
import com.example.adoptr_backend.service.ServiceTypeService;
import com.example.adoptr_backend.service.dto.request.ServiceTypeDTOin;
import com.example.adoptr_backend.service.dto.request.ServiceTypeFilterDTO;
import com.example.adoptr_backend.service.dto.response.ServiceTypeDTO;
import com.example.adoptr_backend.service.mapper.ServiceTypeMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ServiceTypeImpl implements ServiceTypeService {
    private final ServiceTypeRepository serviceTypeRepository;

    public ServiceTypeImpl(ServiceTypeRepository serviceTypeRepository) {
        this.serviceTypeRepository = serviceTypeRepository;
    }
    @Override
    public ServiceTypeDTO create(ServiceTypeDTOin dto) {
        ServiceType serviceType = ServiceTypeMapper.MAPPER.toEntity(dto);
        serviceType = serviceTypeRepository.save(serviceType);
        return ServiceTypeMapper.MAPPER.toDto(serviceType);
    }

    @Override
    public Page<ServiceTypeDTO> getAll(ServiceTypeFilterDTO filter, Pageable pageable) {
        Specification<ServiceType> spec = ServiceTypeSpec.getSpec(filter);
        Page<ServiceType> page = serviceTypeRepository.findAll(spec, pageable);
        return page.map(ServiceTypeMapper.MAPPER::toDto);
    }
}
