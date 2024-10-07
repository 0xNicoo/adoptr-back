package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.Adoption;
import com.example.adoptr_backend.model.ImageType;
import com.example.adoptr_backend.model.ServiceType;
import com.example.adoptr_backend.repository.ServiceTypeRepository;
import com.example.adoptr_backend.repository.UserRepository;
import com.example.adoptr_backend.repository.specification.ServiceTypeSpec;
import com.example.adoptr_backend.service.ImageService;
import com.example.adoptr_backend.service.ServiceTypeService;
import com.example.adoptr_backend.service.dto.request.ServiceTypeDTOin;
import com.example.adoptr_backend.service.dto.request.ServiceTypeFilterDTO;
import com.example.adoptr_backend.service.dto.response.AdoptionDTO;
import com.example.adoptr_backend.service.dto.response.ServiceDTO;
import com.example.adoptr_backend.service.dto.response.ServiceTypeDTO;
import com.example.adoptr_backend.service.mapper.AdoptionMapper;
import com.example.adoptr_backend.service.mapper.ServiceMapper;
import com.example.adoptr_backend.service.mapper.ServiceTypeMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    @Override
    public ServiceTypeDTO update(Long id, ServiceTypeDTOin dto) {
        ServiceType serviceType = getServiceType(id);
        ServiceType serviceTypeUpdated = ServiceTypeMapper.MAPPER.toEntity(dto);
        ServiceTypeMapper.MAPPER.update(serviceType, serviceTypeUpdated);
        if (dto.getImage() != null) {
            imageService.deleteImage(serviceType.getId(), ImageType.SERVICE_TYPE);
            Long imageId = imageService.uploadServiceTypeImage(dto.getImage(), serviceType.getId());
            serviceType.setImageId(imageId);
        }
        serviceTypeRepository.save(serviceType);
        ServiceTypeDTO serviceTypeDTO = ServiceTypeMapper.MAPPER.toDto(serviceType);
        serviceTypeDTO.setS3Url(imageService.getS3url(serviceType.getId(), ImageType.SERVICE_TYPE));
        return serviceTypeDTO;

    }

    @Override
    public ServiceTypeDTO getById(Long id) {
        ServiceType serviceType = getServiceType(id);
        ServiceTypeDTO dto = ServiceTypeMapper.MAPPER.toDto(serviceType);
        String s3Url = imageService.getS3url(id, ImageType.SERVICE_TYPE);
        dto.setS3Url(s3Url);
        return dto;
    }



    private ServiceType getServiceType(Long id) {
        Optional<ServiceType> serviceTypeOptional = serviceTypeRepository.findById(id);
        if(serviceTypeOptional.isEmpty()){
            throw new BadRequestException(Error.SERVICE_TYPE_NOT_FOUND);
        }
        return serviceTypeOptional.get();
    }
}
