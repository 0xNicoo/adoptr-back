package com.adoptr.adoptr.chat.dto.response;

import java.util.List;

/**
 * Tramo del historial, del mensaje más viejo al más nuevo. {@code hasMore} indica que hay mensajes anteriores:
 * se piden con {@code before} = id del primer mensaje de {@code items}.
 */
public record MessagePageDTO(List<MessageDTO> items, boolean hasMore) {
}
