package com.example.adoptr_backend.exception.error.type;

import com.example.adoptr_backend.exception.error.ErrorCode;

public enum ExampleError implements ErrorCode {

    EXAMPLE_NOT_FOUND("0001","No se encontro el ejemplo");

    private final String code;

    private final String message;

    ExampleError(String code, String message) {
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
