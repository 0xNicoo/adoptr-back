package com.adoptr.adoptr.shared.storage;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

public enum ImageError implements ErrorCode {

    INVALID("IMAGE_INVALID", "IMAGE", "La imagen debe ser un archivo JPEG, PNG o WebP válido"),
    TOO_LARGE("IMAGE_TOO_LARGE", "IMAGE", "La imagen es demasiado grande: hasta 5 MB y 40 megapíxeles"),
    STORAGE_UNAVAILABLE("IMAGE_STORAGE_UNAVAILABLE", "IMAGE", "No se pudo guardar la imagen. Probá de nuevo más tarde");

    private final String code;

    private final String model;

    private final String message;

    ImageError(String code, String model, String message) {
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
