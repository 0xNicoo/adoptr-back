package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Chat;
import com.example.adoptr_backend.service.dto.response.ChatDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatMapper extends EntityMapper<ChatDTO, Chat> {
    ChatMapper MAPPER = Mappers.getMapper(ChatMapper.class);
}
