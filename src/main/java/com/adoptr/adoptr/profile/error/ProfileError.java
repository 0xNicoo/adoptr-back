package com.adoptr.adoptr.profile.error;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

public enum ProfileError implements ErrorCode {

    NOT_FOUND("PROFILE_NOT_FOUND", "PROFILE", "No se encontró el perfil"),
    ALREADY_EXISTS("PROFILE_ALREADY_EXISTS", "PROFILE", "El usuario ya tiene un perfil"),
    REQUIRED("PROFILE_REQUIRED", "PROFILE", "Necesitás crear tu perfil para realizar esta acción"),
    PHOTO_NOT_FOUND("PROFILE_PHOTO_NOT_FOUND", "PROFILE", "El perfil no tiene foto"),
    PHOTO_CONFLICT("PROFILE_PHOTO_CONFLICT", "PROFILE", "Se está subiendo otra foto de perfil. Probá de nuevo");

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
