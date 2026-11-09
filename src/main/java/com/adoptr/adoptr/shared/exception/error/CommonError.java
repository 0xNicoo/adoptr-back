package com.adoptr.adoptr.shared.exception.error;

/**
 * Errores transversales a todos los módulos. El código es el nombre de la
 * constante: único y estable para que el front pueda distinguirlos. Los
 * mensajes son constantes y seguros de mostrar: nunca incluyen detalles internos.
 */
public enum CommonError implements ErrorCode {
    INTERNAL_ERROR("Error interno del servidor"),
    UNAUTHENTICATED("Autenticación requerida o inválida"),
    ACCESS_DENIED("No tenés permisos para realizar esta operación"),
    VALIDATION_ERROR("Los datos enviados no son válidos"),
    MALFORMED_REQUEST("El cuerpo de la solicitud no se pudo interpretar"),
    ROUTE_NOT_FOUND("Recurso no encontrado"),
    METHOD_NOT_ALLOWED("Método no permitido"),
    UNSUPPORTED_MEDIA_TYPE("Tipo de contenido no soportado");

    private final String message;

    CommonError(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getModel() {
        return "COMMON";
    }
}
