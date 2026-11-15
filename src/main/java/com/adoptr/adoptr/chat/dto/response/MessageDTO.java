package com.adoptr.adoptr.chat.dto.response;

import com.adoptr.adoptr.chat.model.ChatMessage;
import com.adoptr.adoptr.chat.model.Conversation;
import com.adoptr.adoptr.chat.model.ChatRole;

import java.time.LocalDateTime;

/**
 * Mensaje de una conversación. {@code senderRole} es el mismo para los dos participantes (no depende de quién
 * mira), así el mismo evento en tiempo real sirve para ambos: cada uno lo compara con su {@code role}.
 */
public record MessageDTO(Long id, Long conversationId, ChatRole senderRole, String body, LocalDateTime createdAt) {

    public static MessageDTO of(ChatMessage message, Conversation conversation) {
        return new MessageDTO(message.getId(), conversation.getId(),
                conversation.roleOf(message.getSender().getId()), message.getBody(), message.getCreatedAt());
    }
}
