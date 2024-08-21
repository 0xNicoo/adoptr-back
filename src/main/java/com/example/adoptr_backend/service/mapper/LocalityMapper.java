package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Locality;
import com.example.adoptr_backend.service.dto.request.LocalityDTOin;
import com.example.adoptr_backend.service.dto.response.LocalityDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocalityMapper extends EntityMapper<LocalityDTO, Locality>{
    LocalityMapper MAPPER = Mappers.getMapper(LocalityMapper.class);
    Locality toEntity(LocalityDTOin dto);
}
