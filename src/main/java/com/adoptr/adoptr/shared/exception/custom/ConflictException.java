package com.adoptr.adoptr.shared.exception.custom;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

/** La operación choca con el estado actual del recurso (por ejemplo, ya existe). Responde 409. */
public class ConflictException extends ServiceException {
    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }
}
