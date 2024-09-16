package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.*;
import com.example.adoptr_backend.repository.*;
import com.example.adoptr_backend.service.ImageService;
import com.example.adoptr_backend.service.ServicesService;
import com.example.adoptr_backend.service.dto.request.ServiceDTOin;
import com.example.adoptr_backend.service.dto.response.ServiceDTO;
import com.example.adoptr_backend.service.mapper.ServiceMapper;
import com.example.adoptr_backend.util.AuthSupport;

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
