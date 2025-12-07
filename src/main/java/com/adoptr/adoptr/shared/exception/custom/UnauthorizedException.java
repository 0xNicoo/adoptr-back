package com.adoptr.adoptr.shared.exception.custom;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

public class UnauthorizedException extends ServiceException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnauthorizedException(String code, String message) {
        super(code, message);
    }
}
