package com.adoptr.adoptr.location.error;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

public enum LocationError implements ErrorCode {

    PROVINCE_NOT_FOUND("PROVINCE_NOT_FOUND", "LOCATION", "No se encontró la provincia");

    private final String code;

    private final String model;

    private final String message;

    LocationError(String code, String model, String message) {
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
