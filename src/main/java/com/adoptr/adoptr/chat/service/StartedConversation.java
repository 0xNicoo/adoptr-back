package com.adoptr.adoptr.chat.service;

import com.adoptr.adoptr.chat.dto.response.ConversationDTO;

/** Resultado de escribir sobre una publicación: la conversación y si se creó con este mensaje. */
public record StartedConversation(ConversationDTO conversation, boolean created) {
}
