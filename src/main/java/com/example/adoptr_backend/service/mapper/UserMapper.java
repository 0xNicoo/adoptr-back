package com.example.adoptr_backend.service.mapper;


import com.example.adoptr_backend.model.User;
import com.example.adoptr_backend.service.dto.request.UserDTOin;
import com.example.adoptr_backend.service.dto.response.UserDTO;
import org.mapstruct.Mapper;

import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends EntityMapper<UserDTO, User>{
    UserMapper MAPPER = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "password", source = "dto", qualifiedByName = "encodePassword")
    User toEntity(UserDTOin dto);

    @Named("encodePassword")
    default String getBusinessConsumer(UserDTOin dto){
        return PasswordEncodedUtil.encode(dto.getPassword());
    }
}
