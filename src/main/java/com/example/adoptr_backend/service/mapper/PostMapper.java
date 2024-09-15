package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Post;
import com.example.adoptr_backend.service.dto.request.PostDTOin;
import com.example.adoptr_backend.service.dto.response.PostDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper extends EntityMapper<PostDTO, Post>{

    PostMapper MAPPER = Mappers.getMapper(PostMapper.class);

    Post toEntity(PostDTOin dto);

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "description")
    void update(@MappingTarget Post entity, Post updateEntity);
}
