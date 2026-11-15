package com.adoptr.adoptr.chat.event;

import com.adoptr.adoptr.chat.dto.response.MessageDTO;

import java.util.List;

/** Se publica al guardar un mensaje; {@link ChatNotifier} lo empuja a los participantes cuando confirma la transacción. */
public record MessageSentEvent(List<Long> recipientUserIds, MessageDTO message) {
}
