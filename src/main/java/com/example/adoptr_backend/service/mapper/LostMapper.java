package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Lost;
import com.example.adoptr_backend.service.dto.request.LostDTOin;
import com.example.adoptr_backend.service.dto.response.LostDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LostMapper extends EntityMapper<LostDTO, Lost>{
    LostMapper MAPPER = Mappers.getMapper(LostMapper.class);
    Lost toEntity(LostDTOin dto);

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "user")
    @Mapping(ignore = true, target = "imageId")
    void update(@MappingTarget Lost entity, Lost updateEntity);
}

