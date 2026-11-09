package com.adoptr.adoptr.shared.exception;

import com.adoptr.adoptr.profile.error.ProfileError;
import com.adoptr.adoptr.shared.exception.custom.ForbiddenException;
import com.adoptr.adoptr.shared.exception.error.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void forbiddenExceptionDevuelve403ConSuCodigo() {
        ResponseEntity<ApiError> response = handler.forbidden(new ForbiddenException(ProfileError.REQUIRED));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getCode()).isEqualTo("PROFILE_REQUIRED");
        assertThat(response.getBody().getMessage()).isEqualTo("Necesitás crear tu perfil para realizar esta acción");
    }

    @Test
    void unaSubidaQueSuperaElLimiteDeSpringDevuelve413() {
        ResponseEntity<ApiError> response = handler.uploadTooLarge(new MaxUploadSizeExceededException(5));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE);
        assertThat(response.getBody().getCode()).isEqualTo("IMAGE_TOO_LARGE");
    }

    @Test
    void unMultipartIlegibleDevuelve400() {
        ResponseEntity<ApiError> response = handler.malformedMultipart(new MultipartException("detalle interno"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("MALFORMED_REQUEST");
        assertThat(response.getBody().getMessage()).doesNotContain("detalle interno");
    }
}
