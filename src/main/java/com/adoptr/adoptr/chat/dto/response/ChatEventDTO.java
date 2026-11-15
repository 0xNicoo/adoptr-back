package com.adoptr.adoptr.chat.dto.response;

/** Evento que se empuja por WebSocket a cada participante. Hoy el único tipo es {@code MESSAGE}. */
public record ChatEventDTO(String type, MessageDTO message) {

    public static final String MESSAGE = "MESSAGE";

    public static ChatEventDTO message(MessageDTO message) {
        return new ChatEventDTO(MESSAGE, message);
    }
}
