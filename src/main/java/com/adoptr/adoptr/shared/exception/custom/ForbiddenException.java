package com.adoptr.adoptr.shared.exception.custom;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

/** La persona está autenticada pero no puede realizar la operación (403). */
public class ForbiddenException extends ServiceException {

    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
