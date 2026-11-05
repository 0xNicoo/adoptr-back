package com.adoptr.adoptr.shared.security;

import com.adoptr.adoptr.shared.exception.error.CommonError;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Errores de Spring Security (no pasan por GlobalExceptionHandler) con el mismo
 * formato {@code {"code","message"}} que el resto de la API. Los mensajes son
 * constantes sin comillas, por eso se arma el JSON sin escapar.
 */
public final class ApiErrorHandlers {

    private ApiErrorHandlers() {
    }

    public static AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                write(response, HttpServletResponse.SC_UNAUTHORIZED, CommonError.UNAUTHENTICATED);
    }

    public static AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                write(response, HttpServletResponse.SC_FORBIDDEN, CommonError.ACCESS_DENIED);
    }

    private static void write(HttpServletResponse response, int status, CommonError error) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"code\":\"" + error.getCode() + "\",\"message\":\"" + error.getMessage() + "\"}");
    }
}
