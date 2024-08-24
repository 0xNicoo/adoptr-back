package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Adoption;
import com.example.adoptr_backend.service.dto.request.AdoptionDTOin;
import com.example.adoptr_backend.service.dto.response.AdoptionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdoptionMapper extends EntityMapper<AdoptionDTO, Adoption>{
    AdoptionMapper MAPPER = Mappers.getMapper(AdoptionMapper.class);
    Adoption toEntity(AdoptionDTOin dto);

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "user")
    @Mapping(ignore = true, target = "imageId")
    void update(@MappingTarget Adoption entity, Adoption updateEntity);
}