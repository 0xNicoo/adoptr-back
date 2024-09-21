package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.*;
import com.example.adoptr_backend.repository.*;
import com.example.adoptr_backend.repository.specification.ServiceSpec;
import com.example.adoptr_backend.service.ImageService;
import com.example.adoptr_backend.service.ServicesService;
import com.example.adoptr_backend.service.dto.request.AdoptionDTOin;
import com.example.adoptr_backend.service.dto.request.ServiceDTOin;
import com.example.adoptr_backend.service.dto.request.ServiceFilterDTO;
import com.example.adoptr_backend.service.dto.response.AdoptionDTO;
import com.example.adoptr_backend.service.dto.response.ServiceDTO;
import com.example.adoptr_backend.service.mapper.AdoptionMapper;
import com.example.adoptr_backend.service.mapper.ServiceMapper;
import com.example.adoptr_backend.util.AuthSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServicesServiceImpl implements ServicesService {

    private final ServiceRepository serviceRepository;

    private final UserRepository userRepository;

    private final ImageService imageService;

    private final LocalityRepository localityRepository;

    private final ServiceTypeRepository serviceTypeRepository;


    public ServicesServiceImpl(ServiceRepository serviceRepository,
                               UserRepository userRepository,
                               ImageService imageService,
                               LocalityRepository localityRepository,
                               ServiceTypeRepository serviceTypeRepository)
    {
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.localityRepository = localityRepository;
        this.serviceTypeRepository = serviceTypeRepository;
    }

    @Override
    public ServiceDTO create(ServiceDTOin dto) {
        Long userId = AuthSupport.getUserId();
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new BadRequestException(Error.USER_NOT_FOUND);
        }
        Service service =  ServiceMapper.MAPPER.toEntity(dto);
        service.setUser(user.get());
        service.setType(PublicationType.SERVICE);
        service = serviceRepository.save(service);
        Long imageId = imageService.uploadImage(dto.getImage(), ImageType.SERVICE, service.getId());
        service.setImageId(imageId);
        Locality locality = getLocality(dto);
        service.setLocality(locality);
        ServiceType serviceType = getServiceType(dto);
        service.setServiceType(serviceType);
        service = serviceRepository.save(service);
        ServiceDTO serviceDTO = ServiceMapper.MAPPER.toDto(service);
        serviceDTO.setS3Url(imageService.getS3url(service.getId(), ImageType.SERVICE));
        return serviceDTO;
    }

    @Override
    public Page<ServiceDTO> getAll(ServiceFilterDTO filter, Pageable pageable) {
        Specification<Service> spec = ServiceSpec.getSpec(filter);
        Page<Service> page = serviceRepository.findAll(spec, pageable);
        Page<ServiceDTO> dtoPage = page.map(service -> {
            try {
                ServiceDTO dto = ServiceMapper.MAPPER.toDto(service);
                String s3Url = imageService.getS3url(service.getId(), ImageType.SERVICE);
                dto.setS3Url(s3Url);
                return dto;
            } catch (Exception e) {
                System.err.println("Error al obtener la URL de la imagen para ID " + service.getId() + ": " + e.getMessage());
                ServiceDTO dto = ServiceMapper.MAPPER.toDto(service);
                dto.setS3Url(null);
                return dto;
            }
        });
        return dtoPage;
    }

    @Override
    public ServiceDTO getById(Long id) {
        Service service = getService(id);
        ServiceDTO dto = ServiceMapper.MAPPER.toDto(service);
        String s3Url = imageService.getS3url(id, ImageType.SERVICE);
        dto.setS3Url(s3Url);
        return dto;
    }

    @Override
    public ServiceDTO update(Long id, ServiceDTOin dto) {
        Long userId = AuthSupport.getUserId();
        Service service = getService(id);
        if(!Objects.equals(service.getUser().getId(), userId)){
            throw new BadRequestException(Error.USER_NOT_SERVICE_OWNER);
        }
        Service serviceUpdated = ServiceMapper.MAPPER.toEntity(dto);
        ServiceMapper.MAPPER.update(service, serviceUpdated);
        if (dto.getImage() != null) {
            imageService.deleteImage(service.getId(), ImageType.SERVICE);
            Long imageId = imageService.uploadImage(dto.getImage(), ImageType.SERVICE, service.getId());
            service.setImageId(imageId);
            serviceRepository.save(service);
        }
        Locality locality = getLocality(dto);
        service.setLocality(locality);
        serviceRepository.save(service);
        ServiceType serviceType = getServiceType(dto);
        service.setServiceType(serviceType);
        serviceRepository.save(service);
        ServiceDTO serviceDTO = ServiceMapper.MAPPER.toDto(service);
        serviceDTO.setS3Url(imageService.getS3url(service.getId(), ImageType.SERVICE));
        return serviceDTO;
    }

    @Override
    public void delete(Long id)  {
        Long userId = AuthSupport.getUserId();
        Service service = getService(id);
        if(!Objects.equals(service.getUser().getId(), userId)){
            throw new BadRequestException(Error.USER_NOT_SERVICE_OWNER);
        }
        serviceRepository.delete(service);
    }

    private Service getService(Long id) {
        Optional<Service> serviceOptional = serviceRepository.findById(id);
        if (serviceOptional.isEmpty()) {
            throw new BadRequestException(Error.SERVICE_NOT_FOUND);
        }
        return serviceOptional.get();
    }

    private Locality getLocality(ServiceDTOin dto) {
        Optional<Locality> localityOptional = localityRepository.findById(dto.getLocality_id());
        if (localityOptional.isEmpty()) {
            throw new BadRequestException(Error.LOCALITY_NOT_FOUND);
        }
        Locality locality = localityOptional.get();
        locality.setId(dto.getLocality_id());
        return locality;
    }

    private ServiceType getServiceType(ServiceDTOin dto) {
        Optional<ServiceType> serviceTypeOptional = serviceTypeRepository.findById(dto.getServiceType_id());
        if (serviceTypeOptional.isEmpty()) {
            throw new BadRequestException(Error.SERVICE_TYPE_NOT_FOUND);
        }
        ServiceType serviceType = serviceTypeOptional.get();
        serviceType.setId(dto.getServiceType_id());
        return serviceType;
    }
}
