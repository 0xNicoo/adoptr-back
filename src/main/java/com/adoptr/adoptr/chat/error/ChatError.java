package com.adoptr.adoptr.chat.error;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

public enum ChatError implements ErrorCode {

    CONVERSATION_NOT_FOUND("CHAT_CONVERSATION_NOT_FOUND", "CHAT", "No se encontró la conversación"),
    OWN_PUBLICATION("CHAT_OWN_PUBLICATION", "CHAT", "No podés iniciar una conversación sobre tu propia publicación"),
    READ_ONLY("CHAT_READ_ONLY", "CHAT",
            "La publicación ya no está en adopción: la conversación es de solo lectura"),
    CONFLICT("CHAT_CONFLICT", "CHAT", "Se está iniciando otra conversación sobre esta publicación. Probá de nuevo");

    private final String code;

    private final String model;

    private final String message;

    ChatError(String code, String model, String message) {
        this.code = code;
        this.model = model;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getModel() {
        return model;
    }
}
