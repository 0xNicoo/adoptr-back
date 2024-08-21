package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Province;
import com.example.adoptr_backend.service.dto.request.ProvinceDTOin;
import com.example.adoptr_backend.service.dto.response.ProvinceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProvinceMapper extends EntityMapper<ProvinceDTO, Province> {
    ProvinceMapper MAPPER = Mappers.getMapper(ProvinceMapper.class);
    Province toEntity(ProvinceDTOin dto);
}
