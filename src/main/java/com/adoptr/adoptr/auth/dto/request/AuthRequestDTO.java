package com.adoptr.adoptr.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthRequestDTO(
        @NotBlank(message = "es obligatorio") String provider,
        @NotBlank(message = "es obligatorio") String token) {
}
