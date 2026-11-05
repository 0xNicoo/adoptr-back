package com.adoptr.adoptr.shared.exception.error;

import lombok.Data;

@Data
public class ApiError {
    private String code;
    private String message;

    public ApiError(ErrorCode errorCode) {
        this(errorCode.getMessage(), errorCode.getCode());
    }

    public ApiError(String message, String code) {
        this.message = message;
        this.code = code;
    }
}