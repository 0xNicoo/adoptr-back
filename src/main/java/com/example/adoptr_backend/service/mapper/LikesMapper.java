package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Likes;
import com.example.adoptr_backend.service.dto.request.LikesDTOin;
import com.example.adoptr_backend.service.dto.response.LikesDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LikesMapper extends EntityMapper<LikesDTO, Likes>{
    LikesMapper MAPPER = Mappers.getMapper(LikesMapper.class);
    Likes toEntity(LikesDTOin dto);
    @Override
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "post.id", target = "postId")
    LikesDTO toDto(Likes entity);

    @Mapping(ignore = true, target = "id")
    void update(@MappingTarget Likes entity, Likes updateEntity);
}
