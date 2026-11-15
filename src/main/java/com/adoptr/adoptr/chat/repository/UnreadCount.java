package com.adoptr.adoptr.chat.repository;

/** Mensajes sin leer de una conversación, para quien consulta. */
public record UnreadCount(Long conversationId, Long count) {
}
