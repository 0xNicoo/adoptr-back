package com.adoptr.adoptr.shared.util;

import com.adoptr.adoptr.shared.exception.custom.UnauthorizedException;
import com.adoptr.adoptr.shared.exception.error.CommonError;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MembershipSupport {

    /**
     * @return el id interno del usuario autenticado (el principal que deja {@code JwtAuthFilter}).
     * @throws UnauthorizedException si no hay un usuario autenticado con id válido.
     */
    public Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new UnauthorizedException(CommonError.UNAUTHENTICATED);
        }
        return userId;
    }
}
