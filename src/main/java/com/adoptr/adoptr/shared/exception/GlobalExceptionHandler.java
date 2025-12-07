package com.adoptr.adoptr.shared.exception;

import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import com.adoptr.adoptr.shared.exception.custom.UnauthorizedException;
import com.adoptr.adoptr.shared.exception.error.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiError> internalServerError(Exception exception) {
        ApiError apiError = new ApiError(exception.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(BadRequestException ex){
        ApiError apiError = new ApiError(ex.getMessage(), ex.getCode());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> unauthorized(UnauthorizedException exception) {
        ApiError apiError = new ApiError(exception.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }
}
