package com.adoptr.adoptr.chat.service;

import com.adoptr.adoptr.chat.dto.request.MessageDTOin;
import com.adoptr.adoptr.chat.dto.request.StartConversationDTOin;
import com.adoptr.adoptr.chat.dto.response.ConversationDTO;
import com.adoptr.adoptr.chat.dto.response.MessageDTO;
import com.adoptr.adoptr.chat.dto.response.MessagePageDTO;
import com.adoptr.adoptr.chat.model.ChatRole;
import com.adoptr.adoptr.shared.dto.PageDTO;

public interface ConversationService {

    /** Escribe sobre una publicación ajena: crea la conversación con este primer mensaje, o lo suma a la existente. */
    StartedConversation start(StartConversationDTOin request);

    /** Bandeja del usuario; {@code role} y {@code publicationId} son filtros opcionales. */
    PageDTO<ConversationDTO> list(ChatRole role, Long publicationId, int page, int pageSize);

    ConversationDTO get(Long conversationId);

    /** Historial hacia atrás: sin {@code before}, los más recientes. */
    MessagePageDTO messages(Long conversationId, Long before, int pageSize);

    MessageDTO send(Long conversationId, MessageDTOin request);

    /** Marca como leído todo lo que hay hasta ahora en la conversación, para quien consulta. */
    void markRead(Long conversationId);
}
