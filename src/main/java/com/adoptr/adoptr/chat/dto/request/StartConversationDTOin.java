package com.adoptr.adoptr.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Primer mensaje sobre una publicación: crea la conversación, o lo suma a la que ya existe. */
@Data
public class StartConversationDTOin {

    @NotNull
    private Long publicationId;

    @NotBlank
    @Size(max = MessageDTOin.MAX_BODY_LENGTH)
    private String body;
}
