package com.adoptr.adoptr.shared.exception.custom;

import com.adoptr.adoptr.shared.exception.error.ErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceException extends RuntimeException {
    protected String code;
    protected String message;

    public ServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public ServiceException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}