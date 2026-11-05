package com.adoptr.adoptr.profile.error;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

public enum ProfileError implements ErrorCode {

    NOT_FOUND("PROFILE_NOT_FOUND", "PROFILE", "No se encontró el perfil");

    private final String code;

    private final String model;

    private final String message;



    ProfileError(String code, String model, String message) {
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
