package com.adoptr.adoptr.shared.exception;

import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import com.adoptr.adoptr.shared.exception.custom.ConflictException;
import com.adoptr.adoptr.shared.exception.custom.NotFoundException;
import com.adoptr.adoptr.shared.exception.custom.ServiceException;
import com.adoptr.adoptr.shared.exception.custom.UnauthorizedException;
import com.adoptr.adoptr.shared.exception.error.ApiError;
import com.adoptr.adoptr.shared.exception.error.CommonError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> internalServerError(Exception exception) {
        log.error("Error no controlado", exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, new ApiError(CommonError.INTERNAL_ERROR));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiError> serviceException(ServiceException exception) {
        log.error("ServiceException sin tipo específico: code={}", exception.getCode(), exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, new ApiError(CommonError.INTERNAL_ERROR));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, new ApiError(ex.getMessage(), ex.getCode()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, new ApiError(ex.getMessage(), ex.getCode()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, new ApiError(ex.getMessage(), ex.getCode()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> unauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, new ApiError(ex.getMessage(), ex.getCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        String message = detail.isEmpty()
                ? CommonError.VALIDATION_ERROR.getMessage()
                : CommonError.VALIDATION_ERROR.getMessage() + " (" + detail + ")";
        return build(HttpStatus.BAD_REQUEST, new ApiError(message, CommonError.VALIDATION_ERROR.getCode()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> typeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = CommonError.VALIDATION_ERROR.getMessage() + " (" + ex.getName() + ": formato inválido)";
        return build(HttpStatus.BAD_REQUEST, new ApiError(message, CommonError.VALIDATION_ERROR.getCode()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, new ApiError(CommonError.MALFORMED_REQUEST));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> routeNotFound(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, new ApiError(CommonError.ROUTE_NOT_FOUND));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> methodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, new ApiError(CommonError.METHOD_NOT_ALLOWED));
    }

    private ResponseEntity<ApiError> build(HttpStatus status, ApiError body) {
        return ResponseEntity.status(status).body(body);
    }
}
