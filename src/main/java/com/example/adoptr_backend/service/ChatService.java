package com.example.adoptr_backend.service;

import com.example.adoptr_backend.controller.ChatController;
import com.example.adoptr_backend.model.Chat;
import com.example.adoptr_backend.model.ChatFilterType;
import com.example.adoptr_backend.model.Publication;
import com.example.adoptr_backend.service.dto.response.ChatDTO;
import com.example.adoptr_backend.service.dto.response.PublicationChatDTO;
import com.example.adoptr_backend.service.dto.response.UserChatDTO;
import com.example.adoptr_backend.util.ChatMessage;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatService {
    ChatDTO get(Long id);

    List<ChatDTO> getPublicationChats(Long publicationId);

    List<PublicationChatDTO> getAllGroupByPublication(ChatFilterType chatFilterType);

    List<UserChatDTO> getAllGroupByUsers();

    List<ChatDTO> getAll();

    void saveMassage(ChatMessage message);

    @Transactional
    Chat createChat(Publication publication, Long adopterUserId);
}
