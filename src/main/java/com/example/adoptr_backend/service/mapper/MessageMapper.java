package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Message;
import com.example.adoptr_backend.service.dto.response.MessageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MessageMapper extends EntityMapper<MessageDTO, Message>{
    MessageMapper MAPPER = Mappers.getMapper(MessageMapper.class);
}
