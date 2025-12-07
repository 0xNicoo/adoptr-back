package com.adoptr.adoptr.shared.exception.custom;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

public class BadRequestException extends ServiceException {
    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BadRequestException(String code, String message) {
        super(code, message);
    }
}