package com.adoptr.adoptr.shared.util;

import com.adoptr.adoptr.shared.exception.custom.UnauthorizedException;
import com.adoptr.adoptr.shared.exception.error.CommonError;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MembershipSupport {

    /**
     * @return el id interno del usuario autenticado (el principal que deja {@code JwtAuthFilter}).
     * @throws UnauthorizedException si no hay un usuario autenticado con id válido.
     */
    public Long getUserId() {
        return findUserId().orElseThrow(() -> new UnauthorizedException(CommonError.UNAUTHENTICATED));
    }

    /**
     * Para rutas públicas que cambian según quién mira: vacío si el request no trae un JWT válido.
     */
    public Optional<Long> findUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return Optional.of(userId);
        }
        return Optional.empty();
    }
}
