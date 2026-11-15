package com.adoptr.adoptr.chat.dto.response;

import com.adoptr.adoptr.adoption.dto.response.OwnerDTO;
import com.adoptr.adoptr.chat.model.ChatRole;

import java.time.LocalDateTime;

/**
 * Conversación vista por quien consulta.
 *
 * @param role         {@code OWNER} si la publicación es de quien mira (lo contactaron), {@code INTERESTED} si inició él
 * @param counterpart  la otra persona, con los mismos datos públicos que el responsable de una publicación
 * @param unreadCount  mensajes de la otra persona que quien mira todavía no marcó como leídos
 * @param readOnly     la publicación ya no está en adopción (adoptada o dada de baja): no se pueden enviar mensajes
 */
public record ConversationDTO(Long id, ChatRole role, ConversationPublicationDTO publication, OwnerDTO counterpart,
                              MessageDTO lastMessage, long unreadCount, boolean readOnly,
                              LocalDateTime createdAt, LocalDateTime lastMessageAt) {
}
