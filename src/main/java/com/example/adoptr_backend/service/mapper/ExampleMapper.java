package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Example;
import com.example.adoptr_backend.service.dto.request.ExampleDTOin;
import com.example.adoptr_backend.service.dto.response.ExampleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExampleMapper extends EntityMapper<ExampleDTO, Example>{
    ExampleMapper MAPPER = Mappers.getMapper(ExampleMapper.class);
    Example toEntity(ExampleDTOin dto);

}
