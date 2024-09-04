package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.response.ChatDTO;
import com.example.adoptr_backend.util.ChatMessage;

import java.util.List;

public interface ChatService {
    ChatDTO get(Long id);

    ChatDTO getByPublication(Long publicationId);

    List<ChatDTO> getAll();

    void saveMassage(ChatMessage message);
}
