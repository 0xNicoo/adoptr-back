package com.adoptr.adoptr.publication.error;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

public enum PublicationError implements ErrorCode {

    NOT_FOUND("001", "PUBLICATION","No se encontró");

    private final String code;

    private final String model;

    private final String message;

    PublicationError(String code, String model, String message) {
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
