package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.response.ChatDTO;
import com.example.adoptr_backend.service.dto.response.ProfileDTO;
import com.example.adoptr_backend.service.dto.response.PublicationChatDTO;
import com.example.adoptr_backend.service.dto.response.UserChatDTO;
import com.example.adoptr_backend.util.ChatMessage;

import java.util.List;

public interface ChatService {
    ChatDTO get(Long id);

    List<ChatDTO> getPublicationChats(Long publicationId);

    List<PublicationChatDTO> getAllGroupByPublication();

    List<UserChatDTO> getAllGroupByUsers();

    List<ChatDTO> getAll();

    void saveMassage(ChatMessage message);
}
