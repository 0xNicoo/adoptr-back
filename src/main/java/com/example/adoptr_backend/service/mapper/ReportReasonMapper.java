package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.ReportReason;
import com.example.adoptr_backend.service.dto.response.ReportReasonDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReportReasonMapper extends EntityMapper<ReportReasonDTO, ReportReason> {
    ReportReasonMapper MAPPER = Mappers.getMapper(ReportReasonMapper.class);
}
