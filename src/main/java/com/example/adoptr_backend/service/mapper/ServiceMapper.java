package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Service;
import com.example.adoptr_backend.service.dto.request.ServiceDTOin;
import com.example.adoptr_backend.service.dto.response.ServiceDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ServiceMapper extends EntityMapper<ServiceDTO, Service>{
    ServiceMapper MAPPER = Mappers.getMapper(ServiceMapper.class);
    Service toEntity(ServiceDTOin dto);

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "user")
    @Mapping(ignore = true, target = "imageId")
    void update(@MappingTarget Service entity, Service updateEntity);
}
