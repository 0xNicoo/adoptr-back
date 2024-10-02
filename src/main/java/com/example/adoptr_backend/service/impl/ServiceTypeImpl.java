package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.model.ImageType;
import com.example.adoptr_backend.model.ServiceType;
import com.example.adoptr_backend.repository.ServiceTypeRepository;
import com.example.adoptr_backend.repository.UserRepository;
import com.example.adoptr_backend.repository.specification.ServiceTypeSpec;
import com.example.adoptr_backend.service.ImageService;
import com.example.adoptr_backend.service.ServiceTypeService;
import com.example.adoptr_backend.service.dto.request.ServiceTypeDTOin;
import com.example.adoptr_backend.service.dto.request.ServiceTypeFilterDTO;
import com.example.adoptr_backend.service.dto.response.ServiceDTO;
import com.example.adoptr_backend.service.dto.response.ServiceTypeDTO;
import com.example.adoptr_backend.service.mapper.ServiceMapper;
import com.example.adoptr_backend.service.mapper.ServiceTypeMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ServiceTypeImpl implements ServiceTypeService {
    private final ServiceTypeRepository serviceTypeRepository;

    private final UserRepository userRepository;

    private final ImageService imageService;

    public ServiceTypeImpl(ServiceTypeRepository serviceTypeRepository,
                           UserRepository userRepository,
                           ImageService imageService) {
        this.serviceTypeRepository = serviceTypeRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
    }
    @Override
    public ServiceTypeDTO create(ServiceTypeDTOin dto) {
        ServiceType serviceType = ServiceTypeMapper.MAPPER.toEntity(dto);
        serviceType = serviceTypeRepository.save(serviceType);
        Long imageId = imageService.uploadServiceTypeImage(dto.getImage(), serviceType.getId());
        serviceType.setImageId(imageId);
        serviceType = serviceTypeRepository.save(serviceType);
        ServiceTypeDTO serviceTypeDTO = ServiceTypeMapper.MAPPER.toDto(serviceType);
        serviceTypeDTO.setS3Url(imageService.getS3url(serviceType.getId(), ImageType.SERVICE_TYPE));
        return serviceTypeDTO;
    }

    @Override
    public Page<ServiceTypeDTO> getAll(ServiceTypeFilterDTO filter, Pageable pageable) {
        Specification<ServiceType> spec = ServiceTypeSpec.getSpec(filter);
        Page<ServiceType> page = serviceTypeRepository.findAll(spec, pageable);
        Page<ServiceTypeDTO> dtoPage = page.map(serviceType -> {
            try {
                ServiceTypeDTO dto = ServiceTypeMapper.MAPPER.toDto(serviceType);
                String s3Url = imageService.getS3url(serviceType.getId(), ImageType.SERVICE_TYPE);
                dto.setS3Url(s3Url);
                return dto;
            } catch (Exception e) {
                System.err.println("Error al obtener la URL de la imagen para ID " + serviceType.getId() + ": " + e.getMessage());
                ServiceTypeDTO dto = ServiceTypeMapper.MAPPER.toDto(serviceType);
                dto.setS3Url(null);
                return dto;
            }
        });
        return dtoPage;
    }
}
