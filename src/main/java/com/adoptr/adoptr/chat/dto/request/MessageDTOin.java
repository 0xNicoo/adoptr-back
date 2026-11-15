package com.adoptr.adoptr.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageDTOin {

    public static final int MAX_BODY_LENGTH = 2000;

    @NotBlank
    @Size(max = MAX_BODY_LENGTH)
    private String body;
}
