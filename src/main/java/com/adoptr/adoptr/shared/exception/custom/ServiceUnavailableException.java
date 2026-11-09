package com.adoptr.adoptr.shared.exception.custom;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;

/** Un servicio externo necesario para la operación no está disponible. Responde 503. */
public class ServiceUnavailableException extends ServiceException {
    public ServiceUnavailableException(ErrorCode errorCode) {
        super(errorCode);
    }
}
