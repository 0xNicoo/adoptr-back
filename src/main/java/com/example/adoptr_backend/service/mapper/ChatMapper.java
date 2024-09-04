package com.example.adoptr_backend.service.mapper;

import com.example.adoptr_backend.model.Chat;
import com.example.adoptr_backend.service.dto.response.ChatDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatMapper {
    ChatMapper MAPPER = Mappers.getMapper(ChatMapper.class);

    ChatDTO toDto(Chat chat);
    List<ChatDTO> toDto(List<Chat> chats);
}