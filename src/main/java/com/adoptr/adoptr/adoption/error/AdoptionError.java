package com.adoptr.adoptr.adoption.error;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

public enum AdoptionError implements ErrorCode {

    NOT_FOUND("ADOPTION_NOT_FOUND", "ADOPTION", "No se encontró la publicación de adopción"),
    NOT_OWNER("ADOPTION_NOT_OWNER", "ADOPTION", "Solo quien publicó puede modificar esta publicación"),
    PHOTO_NOT_FOUND("ADOPTION_PHOTO_NOT_FOUND", "ADOPTION", "No se encontró la foto"),
    PHOTO_CONFLICT("ADOPTION_PHOTO_CONFLICT", "ADOPTION", "Se está subiendo otra foto de esta publicación. Probá de nuevo"),
    PHOTO_LIMIT_REACHED("ADOPTION_PHOTO_LIMIT_REACHED", "ADOPTION", "La publicación ya tiene el máximo de 5 fotos");

    private final String code;

    private final String model;

    private final String message;

    AdoptionError(String code, String model, String message) {
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
