package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Example;
import com.example.adoptr_backend.model.Profile;
import com.example.adoptr_backend.service.dto.request.ExampleDTOin;
import com.example.adoptr_backend.service.dto.request.ProfileDTOin;
import com.example.adoptr_backend.service.dto.response.ProfileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProfileMapper extends EntityMapper<ProfileDTO, Profile>{

    ProfileMapper MAPPER = Mappers.getMapper(ProfileMapper.class);

    Profile toEntity(ProfileDTOin dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(ignore = true, target = "user")
    void update(@MappingTarget Profile entity, Profile updateEntity);
}
