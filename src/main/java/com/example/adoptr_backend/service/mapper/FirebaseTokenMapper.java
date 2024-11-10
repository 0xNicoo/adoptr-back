package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.FirebaseToken;
import com.example.adoptr_backend.service.dto.response.FirebaseTokenDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FirebaseTokenMapper extends EntityMapper<FirebaseTokenDTO, FirebaseToken>{
    FirebaseTokenMapper MAPPER = Mappers.getMapper(FirebaseTokenMapper.class);
}
