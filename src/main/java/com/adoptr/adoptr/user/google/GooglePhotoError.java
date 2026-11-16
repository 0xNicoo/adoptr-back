package com.adoptr.adoptr.user.google;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

public enum GooglePhotoError implements ErrorCode {

    NOT_FOUND("GOOGLE_PHOTO_NOT_FOUND", "USER", "Tu cuenta de Google no tiene foto"),
    UNAVAILABLE("GOOGLE_PHOTO_UNAVAILABLE", "USER", "No pudimos traer tu foto de Google. Probá de nuevo");

    private final String code;

    private final String model;

    private final String message;

    GooglePhotoError(String code, String model, String message) {
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
