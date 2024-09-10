package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Favorite;
import com.example.adoptr_backend.service.dto.response.FavoriteDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FavoriteMapper extends EntityMapper<FavoriteDTO, Favorite>{
    FavoriteMapper MAPPER = Mappers.getMapper(FavoriteMapper.class);
}
