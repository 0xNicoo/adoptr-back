package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Notification;
import com.example.adoptr_backend.service.dto.response.NotificationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper extends EntityMapper<NotificationDTO, Notification>{
    NotificationMapper MAPPER = Mappers.getMapper(NotificationMapper.class);
}
