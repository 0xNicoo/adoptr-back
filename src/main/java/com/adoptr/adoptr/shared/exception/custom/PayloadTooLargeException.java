package com.adoptr.adoptr.shared.exception.custom;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

/** El contenido enviado supera los límites aceptados. Responde 413. */
public class PayloadTooLargeException extends ServiceException {
    public PayloadTooLargeException(ErrorCode errorCode) {
        super(errorCode);
    }
}
