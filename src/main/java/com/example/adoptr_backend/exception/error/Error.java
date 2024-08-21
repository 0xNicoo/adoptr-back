package com.example.adoptr_backend.exception.error;

public enum Error implements ErrorCode {

    EXAMPLE_NOT_FOUND("0001","No se encontro el ejemplo"),
    PROFILE_ALREADY_EXIST("0002", "Este usuario ya tiene un perfil"),
    AUTH_ERROR("0003", "Error al iniciar sesion"),
    PROVINCE_NOT_FOUND("0004", "No se encontro la provincia")

    ;

    private final String code;

    private final String message;

    Error(String code, String message) {
        this.code = code;
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
}
