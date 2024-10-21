package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Report;
import com.example.adoptr_backend.service.dto.response.ReportDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReportMapper extends EntityMapper<ReportDTO, Report> {
    ReportMapper MAPPER = Mappers.getMapper(ReportMapper.class);
}
