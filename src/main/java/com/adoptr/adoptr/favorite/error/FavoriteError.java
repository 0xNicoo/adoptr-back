package com.adoptr.adoptr.favorite.error;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

public enum FavoriteError implements ErrorCode {

    OWN_PUBLICATION("FAVORITE_OWN_PUBLICATION", "FAVORITE", "No podés guardar tus propias publicaciones");

    private final String code;

    private final String model;

    private final String message;

    FavoriteError(String code, String model, String message) {
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
