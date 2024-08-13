package com.example.adoptr_backend.service.mapper;


import org.mapstruct.Mapper;

import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper{
    UserMapper MAPPER = Mappers.getMapper(UserMapper.class);
}
