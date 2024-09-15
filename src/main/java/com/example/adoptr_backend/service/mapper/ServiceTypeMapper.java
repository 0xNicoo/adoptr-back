package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.ServiceType;
import com.example.adoptr_backend.service.dto.request.ServiceTypeDTOin;
import com.example.adoptr_backend.service.dto.response.ServiceTypeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceTypeMapper extends EntityMapper<ServiceTypeDTO, ServiceType> {
    ServiceTypeMapper MAPPER = Mappers.getMapper(ServiceTypeMapper.class);
    ServiceType toEntity(ServiceTypeDTOin dto);
}
